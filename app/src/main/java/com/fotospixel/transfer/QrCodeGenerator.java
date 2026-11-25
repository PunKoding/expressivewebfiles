package com.fotospixel.transfer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Pure-Java ISO/IEC 18004 QR Code encoder (Byte mode, ECC Level M, Versions 1-6).
 */
public final class QrCodeGenerator {

    private static final int[] TOTAL_CODEWORDS = {0, 26, 44, 70, 100, 134, 172};
    private static final int[] DATA_CODEWORDS_M = {0, 16, 28, 44, 64, 86, 108};
    private static final int[] NUM_BLOCKS_M = {0, 1, 1, 1, 2, 2, 4};
    private static final int[] EC_PER_BLOCK_M = {0, 10, 16, 26, 18, 24, 16};

    private static final int[] GF_EXP = new int[512];
    private static final int[] GF_LOG = new int[256];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            GF_EXP[i] = x;
            GF_LOG[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0) {
                x ^= 0x11D;
            }
        }
        for (int i = 255; i < 512; i++) {
            GF_EXP[i] = GF_EXP[i - 255];
        }
    }

    private QrCodeGenerator() {
    }

    public static Bitmap generateBitmap(String text, int sizePx, int fgColor, int bgColor) {
        boolean[][] modules = encode(text);
        int n = modules.length;
        int quiet = 4;
        int total = n + quiet * 2;
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(bgColor);

        Paint paint = new Paint();
        paint.setColor(fgColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);

        float cell = (float) sizePx / total;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (modules[r][c]) {
                    float left = (c + quiet) * cell;
                    float top = (r + quiet) * cell;
                    float right = (c + quiet + 1) * cell;
                    float bottom = (r + quiet + 1) * cell;
                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }
        return bmp;
    }

    public static boolean[][] encode(String text) {
        byte[] payload = text.getBytes(StandardCharsets.UTF_8);
        int version = pickVersion(payload.length);
        int size = 17 + 4 * version;

        byte[] dataCodewords = buildDataCodewords(payload, version);
        byte[] fullStream = interleaveWithEc(dataCodewords, version);

        boolean[][] bestMatrix = null;
        int bestPenalty = Integer.MAX_VALUE;

        for (int mask = 0; mask < 8; mask++) {
            boolean[][] isFunc = new boolean[size][size];
            boolean[][] modules = new boolean[size][size];
            drawFunctionPatterns(modules, isFunc, version);
            placeDataBits(modules, isFunc, fullStream);
            applyMaskAndFormat(modules, isFunc, mask);
            int penalty = computePenalty(modules);
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestMatrix = modules;
            }
        }
        return bestMatrix;
    }

    private static int pickVersion(int byteLen) {
        // Byte mode overhead: 4 bits mode + 8 bits count + 4 bits terminator = 16 bits = 2 bytes.
        int needed = byteLen + 2;
        for (int v = 1; v <= 6; v++) {
            if (needed <= DATA_CODEWORDS_M[v]) {
                return v;
            }
        }
        throw new IllegalArgumentException("Text too long for QR v1-v6 ECC M: " + byteLen + " bytes");
    }

    private static byte[] buildDataCodewords(byte[] payload, int version) {
        int capacity = DATA_CODEWORDS_M[version];
        BitBuffer bb = new BitBuffer(capacity * 8);
        bb.appendBits(0x4, 4); // Byte mode
        bb.appendBits(payload.length, 8);
        for (byte b : payload) {
            bb.appendBits(b & 0xFF, 8);
        }
        int terminatorLen = Math.min(4, capacity * 8 - bb.bitLength);
        bb.appendBits(0, terminatorLen);
        while ((bb.bitLength & 7) != 0) {
            bb.appendBits(0, 1);
        }
        byte[] out = Arrays.copyOf(bb.bytes, capacity);
        int pos = bb.bitLength / 8;
        int padIdx = 0;
        while (pos < capacity) {
            out[pos++] = (byte) ((padIdx % 2 == 0) ? 0xEC : 0x11);
            padIdx++;
        }
        return out;
    }

    private static byte[] interleaveWithEc(byte[] data, int version) {
        int blocks = NUM_BLOCKS_M[version];
        int ecLen = EC_PER_BLOCK_M[version];
        int dataPerBlock = data.length / blocks;
        int[] rsGen = rsGeneratorPoly(ecLen);

        byte[][] dataBlocks = new byte[blocks][dataPerBlock];
        byte[][] ecBlocks = new byte[blocks][ecLen];
        for (int b = 0; b < blocks; b++) {
            System.arraycopy(data, b * dataPerBlock, dataBlocks[b], 0, dataPerBlock);
            ecBlocks[b] = rsRemainder(dataBlocks[b], rsGen);
        }

        byte[] result = new byte[TOTAL_CODEWORDS[version]];
        int idx = 0;
        for (int i = 0; i < dataPerBlock; i++) {
            for (int b = 0; b < blocks; b++) {
                result[idx++] = dataBlocks[b][i];
            }
        }
        for (int i = 0; i < ecLen; i++) {
            for (int b = 0; b < blocks; b++) {
                result[idx++] = ecBlocks[b][i];
            }
        }
        return result;
    }

    private static int gfMul(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return GF_EXP[GF_LOG[a] + GF_LOG[b]];
    }

    private static int[] rsGeneratorPoly(int degree) {
        int[] poly = new int[degree];
        poly[degree - 1] = 1;
        int root = 1;
        for (int i = 0; i < degree; i++) {
            for (int j = 0; j < degree; j++) {
                poly[j] = gfMul(poly[j], root);
                if (j + 1 < degree) {
                    poly[j] ^= poly[j + 1];
                }
            }
            root = gfMul(root, 0x02);
        }
        return poly;
    }

    private static byte[] rsRemainder(byte[] data, int[] gen) {
        int ecLen = gen.length;
        int[] rem = new int[ecLen];
        for (byte b : data) {
            int factor = (b & 0xFF) ^ rem[0];
            System.arraycopy(rem, 1, rem, 0, ecLen - 1);
            rem[ecLen - 1] = 0;
            for (int j = 0; j < ecLen; j++) {
                rem[j] ^= gfMul(gen[j], factor);
            }
        }
        byte[] out = new byte[ecLen];
        for (int i = 0; i < ecLen; i++) {
            out[i] = (byte) rem[i];
        }
        return out;
    }

    private static void drawFunctionPatterns(boolean[][] m, boolean[][] f, int version) {
        int n = m.length;
        drawFinder(m, f, 0, 0);
        drawFinder(m, f, 0, n - 7);
        drawFinder(m, f, n - 7, 0);

        if (version >= 2) {
            drawAlignment(m, f, n - 7, n - 7);
        }

        // Timing strips
        for (int i = 8; i < n - 8; i++) {
            boolean bit = (i % 2 == 0);
            setFunc(m, f, 6, i, bit);
            setFunc(m, f, i, 6, bit);
        }

        // Dark module
        setFunc(m, f, 4 * version + 9, 8, true);

        // Reserve format areas
        for (int i = 0; i <= 8; i++) {
            f[8][i] = true;
            f[i][8] = true;
        }
        for (int i = n - 8; i < n; i++) {
            f[8][i] = true;
        }
        for (int i = n - 7; i < n; i++) {
            f[i][8] = true;
        }
    }

    private static void drawFinder(boolean[][] m, boolean[][] f, int top, int left) {
        int n = m.length;
        for (int dr = -1; dr <= 7; dr++) {
            for (int dc = -1; dc <= 7; dc++) {
                int r = top + dr;
                int c = left + dc;
                if (r < 0 || r >= n || c < 0 || c >= n) continue;
                int dist = Math.max(Math.abs(dr - 3), Math.abs(dc - 3));
                boolean dark = (dist != 2 && dist != 4);
                setFunc(m, f, r, c, dark);
            }
        }
    }

    private static void drawAlignment(boolean[][] m, boolean[][] f, int cr, int cc) {
        for (int dr = -2; dr <= 2; dr++) {
            for (int dc = -2; dc <= 2; dc++) {
                int dist = Math.max(Math.abs(dr), Math.abs(dc));
                setFunc(m, f, cr + dr, cc + dc, dist != 1);
            }
        }
    }

    private static void setFunc(boolean[][] m, boolean[][] f, int r, int c, boolean val) {
        m[r][c] = val;
        f[r][c] = true;
    }

    private static void placeDataBits(boolean[][] m, boolean[][] f, byte[] stream) {
        int n = m.length;
        int totalBits = stream.length * 8;
        int bitIdx = 0;

        for (int right = n - 1; right >= 1; right -= 2) {
            if (right == 6) {
                right = 5;
            }
            for (int vert = 0; vert < n; vert++) {
                boolean upward = (((right + 1) & 2) == 0);
                int r = upward ? (n - 1 - vert) : vert;
                for (int j = 0; j < 2; j++) {
                    int c = right - j;
                    if (!f[r][c]) {
                        boolean bit = false;
                        if (bitIdx < totalBits) {
                            int b = stream[bitIdx >>> 3] & 0xFF;
                            bit = ((b >>> (7 - (bitIdx & 7))) & 1) != 0;
                            bitIdx++;
                        }
                        m[r][c] = bit;
                    }
                }
            }
        }
    }

    private static void applyMaskAndFormat(boolean[][] m, boolean[][] f, int mask) {
        int n = m.length;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!f[r][c] && maskBit(mask, r, c)) {
                    m[r][c] = !m[r][c];
                }
            }
        }

        // ECC Level M = 00
        int fmtData = mask & 0x7;
        int rem = fmtData << 10;
        for (int i = 4; i >= 0; i--) {
            if ((rem & (1 << (i + 10))) != 0) {
                rem ^= (0x537 << i);
            }
        }
        int fmtBits = ((fmtData << 10) | rem) ^ 0x5412;

        // Place format info bits
        for (int i = 0; i <= 5; i++) {
            m[8][i] = ((fmtBits >>> (14 - i)) & 1) != 0;
        }
        m[8][7] = ((fmtBits >>> 8) & 1) != 0;
        m[8][8] = ((fmtBits >>> 7) & 1) != 0;
        m[7][8] = ((fmtBits >>> 6) & 1) != 0;
        for (int i = 0; i <= 5; i++) {
            m[5 - i][8] = ((fmtBits >>> (5 - i)) & 1) != 0;
        }

        for (int i = 0; i <= 6; i++) {
            m[n - 1 - i][8] = ((fmtBits >>> (14 - i)) & 1) != 0;
        }
        for (int i = 0; i <= 7; i++) {
            m[8][n - 8 + i] = ((fmtBits >>> (7 - i)) & 1) != 0;
        }
    }

    private static boolean maskBit(int mask, int r, int c) {
        switch (mask) {
            case 0: return ((r + c) & 1) == 0;
            case 1: return (r & 1) == 0;
            case 2: return (c % 3) == 0;
            case 3: return ((r + c) % 3) == 0;
            case 4: return (((r / 2) + (c / 3)) & 1) == 0;
            case 5: return ((r * c) % 2 + (r * c) % 3) == 0;
            case 6: return (((r * c) % 2 + (r * c) % 3) & 1) == 0;
            case 7: return (((r + c) % 2 + (r * c) % 3) & 1) == 0;
            default: return false;
        }
    }

    private static int computePenalty(boolean[][] m) {
        int n = m.length;
        int score = 0;

        // Rule 1: adjacent modules in row/column
        for (int r = 0; r < n; r++) {
            boolean runColor = m[r][0];
            int runLen = 1;
            for (int c = 1; c < n; c++) {
                if (m[r][c] == runColor) {
                    runLen++;
                } else {
                    if (runLen >= 5) score += 3 + (runLen - 5);
                    runColor = m[r][c];
                    runLen = 1;
                }
            }
            if (runLen >= 5) score += 3 + (runLen - 5);
        }
        for (int c = 0; c < n; c++) {
            boolean runColor = m[0][c];
            int runLen = 1;
            for (int r = 1; r < n; r++) {
                if (m[r][c] == runColor) {
                    runLen++;
                } else {
                    if (runLen >= 5) score += 3 + (runLen - 5);
                    runColor = m[r][c];
                    runLen = 1;
                }
            }
            if (runLen >= 5) score += 3 + (runLen - 5);
        }

        // Rule 2: 2x2 blocks
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n - 1; c++) {
                boolean v = m[r][c];
                if (m[r][c + 1] == v && m[r + 1][c] == v && m[r + 1][c + 1] == v) {
                    score += 3;
                }
            }
        }

        // Rule 3: finder-like pattern 1:1:3:1:1
        for (int r = 0; r < n; r++) {
            for (int c = 0; c <= n - 7; c++) {
                if (m[r][c] && !m[r][c + 1] && m[r][c + 2] && m[r][c + 3] && m[r][c + 4]
                        && !m[r][c + 5] && m[r][c + 6]) {
                    if (hasQuiet4Row(m, r, c - 4) || hasQuiet4Row(m, r, c + 7)) {
                        score += 40;
                    }
                }
            }
        }
        for (int c = 0; c < n; c++) {
            for (int r = 0; r <= n - 7; r++) {
                if (m[r][c] && !m[r + 1][c] && m[r + 2][c] && m[r + 3][c] && m[r + 4][c]
                        && !m[r + 5][c] && m[r + 6][c]) {
                    if (hasQuiet4Col(m, r - 4, c) || hasQuiet4Col(m, r + 7, c)) {
                        score += 40;
                    }
                }
            }
        }

        // Rule 4: dark ratio
        int dark = 0;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (m[r][c]) dark++;
            }
        }
        int total = n * n;
        int pct5 = Math.abs((dark * 100) / total - 50) / 5;
        score += pct5 * 10;
        return score;
    }

    private static boolean hasQuiet4Row(boolean[][] m, int r, int startC) {
        int n = m.length;
        if (startC < 0 || startC + 4 > n) return false;
        for (int i = 0; i < 4; i++) {
            if (m[r][startC + i]) return false;
        }
        return true;
    }

    private static boolean hasQuiet4Col(boolean[][] m, int startR, int c) {
        int n = m.length;
        if (startR < 0 || startR + 4 > n) return false;
        for (int i = 0; i < 4; i++) {
            if (m[startR + i][c]) return false;
        }
        return true;
    }

    private static final class BitBuffer {
        final byte[] bytes;
        int bitLength = 0;

        BitBuffer(int maxBits) {
            this.bytes = new byte[(maxBits + 7) / 8];
        }

        void appendBits(int val, int len) {
            for (int i = len - 1; i >= 0; i--) {
                if (((val >>> i) & 1) != 0) {
                    bytes[bitLength >>> 3] |= (1 << (7 - (bitLength & 7)));
                }
                bitLength++;
            }
        }
    }
}
