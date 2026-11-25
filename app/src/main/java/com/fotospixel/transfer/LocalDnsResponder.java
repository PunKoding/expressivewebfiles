package com.fotospixel.transfer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pure-Java mDNS (RFC 6762) responder + NsdManager beacon so the device can be reached
 * via http://<brand>.files.local (with automatic <brand>2.files.local collision handling).
 */
public final class LocalDnsResponder {

    private static final String TAG = "FotosPixel";
    public static final int MDNS_PORT = 5353;
    public static final String MDNS_GROUP_V4 = "224.0.0.251";

    private final Context context;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private WifiManager.MulticastLock multicastLock;
    private MulticastSocket socket;
    private Thread responderThread;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener nsdListener;

    private volatile String activeHost = "";
    private volatile Set<String> acceptedNames = Collections.emptySet();
    private volatile InetAddress currentIp;
    private volatile int httpPort = 8080;

    public interface HostResolvedCallback {
        void onHostResolved(String hostname);
    }

    public LocalDnsResponder(Context context) {
        this.context = context.getApplicationContext();
    }

    public static String resolveBaseBrand() {
        String combined = ((Build.BRAND == null ? "" : Build.BRAND) + " "
                + (Build.MANUFACTURER == null ? "" : Build.MANUFACTURER) + " "
                + (Build.MODEL == null ? "" : Build.MODEL)).toLowerCase(Locale.US);
        if (combined.contains("pixel") || combined.contains("google")) return "pixel";
        if (combined.contains("nokia") || combined.contains("hmd") || combined.contains("ta-")) return "nokia";
        if (combined.contains("samsung")) return "samsung";
        if (combined.contains("xiaomi") || combined.contains("redmi") || combined.contains("poco")) return "xiaomi";
        if (combined.contains("nothing")) return "nothing";
        if (combined.contains("oppo")) return "oppo";
        if (combined.contains("realme")) return "realme";
        if (combined.contains("honor")) return "honor";
        if (combined.contains("huawei")) return "huawei";
        if (combined.contains("oneplus")) return "oneplus";
        if (combined.contains("motorola") || combined.contains("moto")) return "motorola";
        String fallback = (Build.BRAND != null && !Build.BRAND.isEmpty()) ? Build.BRAND : Build.MANUFACTURER;
        String clean = fallback == null ? "" : fallback.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
        return clean.isEmpty() ? "android" : clean;
    }

    public static String formatHttpUrl(String hostOrIp, int port) {
        if (hostOrIp == null || hostOrIp.isEmpty()) return "";
        return (port == 80) ? ("http://" + hostOrIp) : ("http://" + hostOrIp + ":" + port);
    }

    public String getActiveHost() {
        return activeHost;
    }

    public synchronized void start(final String ipStr, final int port, final HostResolvedCallback callback) {
        stop();
        this.httpPort = port;
        running.set(true);

        final String baseBrand = resolveBaseBrand();
        this.activeHost = baseBrand + ".files.local";
        if (callback != null) {
            callback.onHostResolved(this.activeHost);
        }

        responderThread = new Thread(() -> runResponder(ipStr, port, baseBrand, callback), "FotosPixel-mDNS");
        responderThread.start();
    }

    public synchronized void updateIpIfChanged(String ipStr) {
        try {
            if (ipStr == null || ipStr.isEmpty() || "0.0.0.0".equals(ipStr)) return;
            InetAddress addr = InetAddress.getByName(ipStr);
            if (addr instanceof Inet4Address) {
                InetAddress prev = this.currentIp;
                this.currentIp = addr;
                if (prev != null && !prev.equals(addr) && socket != null && !socket.isClosed()) {
                    sendGratuitousAnnouncement(socket, addr, activeHost);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public synchronized void stop() {
        running.set(false);
        unregisterNsd();
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
        if (responderThread != null) {
            responderThread.interrupt();
            responderThread = null;
        }
        if (multicastLock != null) {
            try {
                if (multicastLock.isHeld()) multicastLock.release();
            } catch (Exception ignored) {
            }
            multicastLock = null;
        }
    }

    private void runResponder(String ipStr, int port, String baseBrand, HostResolvedCallback callback) {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                multicastLock = wm.createMulticastLock("FotosPixel-mDNS");
                multicastLock.setReferenceCounted(false);
                multicastLock.acquire();
            }

            InetAddress localAddr = resolveIpv4Address(ipStr);
            if (localAddr == null) {
                Log.w(TAG, "mDNS: no IPv4 address available");
                return;
            }
            this.currentIp = localAddr;

            MulticastSocket ms = new MulticastSocket(MDNS_PORT);
            ms.setReuseAddress(true);
            ms.setTimeToLive(255);
            InetAddress group = InetAddress.getByName(MDNS_GROUP_V4);
            NetworkInterface nif = NetworkInterface.getByInetAddress(localAddr);
            if (nif != null) {
                try {
                    ms.setNetworkInterface(nif);
                    ms.joinGroup(new InetSocketAddress(group, MDNS_PORT), nif);
                } catch (Exception e) {
                    ms.joinGroup(group);
                }
            } else {
                ms.joinGroup(group);
            }
            this.socket = ms;

            // Probe <brand>.files.local, <brand>2.files.local, ... for collisions on LAN
            String chosenHost = probeAvailableHost(ms, group, localAddr, baseBrand);
            this.activeHost = chosenHost;
            String prefix = chosenHost.substring(0, chosenHost.indexOf(".files.local"));

            Set<String> names = new HashSet<>();
            names.add(chosenHost.toLowerCase(Locale.US));
            names.add((prefix + "-files.local").toLowerCase(Locale.US));
            names.add((prefix + ".local").toLowerCase(Locale.US));
            this.acceptedNames = names;

            if (callback != null) {
                callback.onHostResolved(chosenHost);
            }
            registerNsdBeacon(prefix + "-files", port);
            Log.i(TAG, "mDNS responder ready: http://" + chosenHost + (port == 80 ? "" : (":" + port)) + " -> " + localAddr.getHostAddress());

            sendGratuitousAnnouncement(ms, localAddr, chosenHost);

            byte[] buf = new byte[1500];
            ms.setSoTimeout(0);
            while (running.get() && !ms.isClosed()) {
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                ms.receive(pkt);
                InetAddress ipNow = this.currentIp;
                if (ipNow == null) continue;
                handleIncomingPacket(ms, group, pkt.getData(), pkt.getLength(), pkt.getAddress(), pkt.getPort(), ipNow);
            }
        } catch (IOException e) {
            if (running.get()) {
                Log.w(TAG, "mDNS responder stopped: " + e.getMessage());
            }
        } finally {
            if (multicastLock != null) {
                try {
                    if (multicastLock.isHeld()) multicastLock.release();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String probeAvailableHost(MulticastSocket ms, InetAddress group, InetAddress localIp, String baseBrand) {
        byte[] recvBuf = new byte[1500];
        for (int idx = 1; idx <= 20 && running.get(); idx++) {
            String prefix = (idx == 1) ? baseBrand : (baseBrand + idx);
            String candidate = prefix + ".files.local";
            boolean conflict = false;
            try {
                byte[] q = buildQueryPacket(candidate);
                ms.send(new DatagramPacket(q, q.length, group, MDNS_PORT));
                long deadline = System.currentTimeMillis() + 300L;
                ms.setSoTimeout(120);
                while (System.currentTimeMillis() < deadline && running.get()) {
                    try {
                        DatagramPacket p = new DatagramPacket(recvBuf, recvBuf.length);
                        ms.receive(p);
                        if (p.getAddress() != null && p.getAddress().equals(localIp)) {
                            continue;
                        }
                        if (packetAnswersForHost(p.getData(), p.getLength(), candidate)) {
                            conflict = true;
                            Log.i(TAG, "mDNS collision detected for " + candidate + " from " + p.getAddress());
                            break;
                        }
                    } catch (java.net.SocketTimeoutException ignored) {
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "mDNS probe error for " + candidate + ": " + e.getMessage());
            }
            if (!conflict) {
                return candidate;
            }
        }
        return baseBrand + ".files.local";
    }

    private void handleIncomingPacket(MulticastSocket ms, InetAddress group,
                                      byte[] data, int len, InetAddress senderAddr, int senderPort,
                                      InetAddress localIp) {
        if (len < 12) return;
        int flags = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        // Only answer DNS queries (QR bit == 0)
        if ((flags & 0x8000) != 0) return;

        int id = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int qdCount = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
        if (qdCount <= 0 || qdCount > 32) return;

        int pos = 12;
        byte[] ipBytes = localIp.getAddress();
        if (ipBytes == null || ipBytes.length != 4) return;

        for (int i = 0; i < qdCount && pos + 4 <= len; i++) {
            int[] nextPos = new int[1];
            String qname = readDnsName(data, len, pos, nextPos);
            pos = nextPos[0];
            if (qname == null || pos + 4 > len) break;
            int qtype = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
            int qclass = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
            pos += 4;

            boolean wantUnicast = (qclass & 0x8000) != 0;
            String lower = qname.toLowerCase(Locale.US);
            if ((qtype == 1 || qtype == 255) && acceptedNames.contains(lower)) {
                byte[] resp = buildAResponsePacket(id, lower, ipBytes);
                try {
                    if (wantUnicast && senderAddr != null && senderPort > 0) {
                        ms.send(new DatagramPacket(resp, resp.length, senderAddr, senderPort));
                    }
                    ms.send(new DatagramPacket(resp, resp.length, group, MDNS_PORT));
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void sendGratuitousAnnouncement(MulticastSocket ms, InetAddress localIp, String host) {
        byte[] ipBytes = localIp.getAddress();
        if (ipBytes == null || ipBytes.length != 4 || host == null || host.isEmpty()) return;
        try {
            InetAddress group = InetAddress.getByName(MDNS_GROUP_V4);
            byte[] resp = buildAResponsePacket(0, host, ipBytes);
            ms.send(new DatagramPacket(resp, resp.length, group, MDNS_PORT));
            Thread.sleep(120);
            ms.send(new DatagramPacket(resp, resp.length, group, MDNS_PORT));
        } catch (Exception ignored) {
        }
    }

    public static byte[] buildQueryPacket(String fqdn) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        // Header: ID=0, Flags=0, QDCOUNT=1
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        out.write(0); out.write(1);
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        writeDnsName(out, fqdn);
        out.write(0); out.write(1); // QTYPE = A
        out.write(0); out.write(1); // QCLASS = IN
        return out.toByteArray();
    }

    public static byte[] buildAResponsePacket(int id, String fqdn, byte[] ipv4Bytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(96);
        out.write((id >> 8) & 0xFF);
        out.write(id & 0xFF);
        // Flags: QR=1, AA=1 -> 0x8400
        out.write(0x84); out.write(0x00);
        // QDCOUNT=0, ANCOUNT=1, NSCOUNT=0, ARCOUNT=0
        out.write(0); out.write(0);
        out.write(0); out.write(1);
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        writeDnsName(out, fqdn);
        out.write(0); out.write(1);       // TYPE = A (1)
        out.write(0x80); out.write(0x01); // CLASS = IN + cache flush
        out.write(0); out.write(0); out.write(0); out.write(120); // TTL = 120s
        out.write(0); out.write(4);       // RDLENGTH = 4
        out.write(ipv4Bytes, 0, 4);
        return out.toByteArray();
    }

    public static boolean packetAnswersForHost(byte[] data, int len, String targetHost) {
        if (len < 12 || targetHost == null) return false;
        int flags = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        if ((flags & 0x8000) == 0) return false; // Must be a DNS response
        int qdCount = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
        int anCount = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
        if (anCount <= 0 || anCount > 64) return false;

        int pos = 12;
        for (int i = 0; i < qdCount && pos < len; i++) {
            int[] np = new int[1];
            readDnsName(data, len, pos, np);
            pos = np[0] + 4;
        }
        String targetLower = targetHost.toLowerCase(Locale.US);
        for (int i = 0; i < anCount && pos + 10 <= len; i++) {
            int[] np = new int[1];
            String aname = readDnsName(data, len, pos, np);
            pos = np[0];
            if (aname == null || pos + 10 > len) break;
            int rtype = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
            int rdlen = ((data[pos + 8] & 0xFF) << 8) | (data[pos + 9] & 0xFF);
            pos += 10 + rdlen;
            if (rtype == 1 && targetLower.equals(aname.toLowerCase(Locale.US))) {
                return true;
            }
        }
        return false;
    }

    public static String readDnsName(byte[] data, int len, int startPos, int[] nextPosOut) {
        List<String> labels = new ArrayList<>();
        int pos = startPos;
        int endPos = -1;
        int jumps = 0;
        while (pos < len && jumps++ < 32) {
            int b = data[pos] & 0xFF;
            if (b == 0) {
                if (endPos < 0) endPos = pos + 1;
                break;
            }
            if ((b & 0xC0) == 0xC0) {
                if (pos + 1 >= len) return null;
                int ptr = ((b & 0x3F) << 8) | (data[pos + 1] & 0xFF);
                if (endPos < 0) endPos = pos + 2;
                pos = ptr;
            } else {
                pos++;
                if (pos + b > len) return null;
                labels.add(new String(data, pos, b, StandardCharsets.UTF_8));
                pos += b;
            }
        }
        if (nextPosOut != null && nextPosOut.length > 0) {
            nextPosOut[0] = (endPos >= 0) ? endPos : pos;
        }
        return labels.isEmpty() ? null : String.join(".", labels);
    }

    private static void writeDnsName(ByteArrayOutputStream out, String fqdn) {
        String[] parts = fqdn.split("\\.");
        for (String p : parts) {
            if (p.isEmpty()) continue;
            byte[] b = p.getBytes(StandardCharsets.UTF_8);
            int l = Math.min(63, b.length);
            out.write(l);
            out.write(b, 0, l);
        }
        out.write(0);
    }

    private InetAddress resolveIpv4Address(String ipStr) {
        try {
            if (ipStr != null && !ipStr.isEmpty() && !"0.0.0.0".equals(ipStr)) {
                InetAddress addr = InetAddress.getByName(ipStr);
                if (addr instanceof Inet4Address) return addr;
            }
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback()) continue;
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void registerNsdBeacon(String serviceName, int port) {
        try {
            NsdManager nm = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            if (nm == null) return;
            nsdManager = nm;
            NsdServiceInfo info = new NsdServiceInfo();
            info.setServiceName(serviceName);
            info.setServiceType("_http._tcp.");
            info.setPort(port);
            nsdListener = new NsdManager.RegistrationListener() {
                @Override public void onServiceRegistered(NsdServiceInfo i) {}
                @Override public void onRegistrationFailed(NsdServiceInfo i, int err) {}
                @Override public void onServiceUnregistered(NsdServiceInfo i) {}
                @Override public void onUnregistrationFailed(NsdServiceInfo i, int err) {}
            };
            nm.registerService(info, NsdManager.PROTOCOL_DNS_SD, nsdListener);
        } catch (Exception ignored) {
        }
    }

    private void unregisterNsd() {
        try {
            if (nsdManager != null && nsdListener != null) {
                nsdManager.unregisterService(nsdListener);
            }
        } catch (Exception ignored) {
        }
        nsdListener = null;
    }
}
