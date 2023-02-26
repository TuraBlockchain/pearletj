package hk.zdl.crypto.pearlet.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.formdev.flatlaf.util.SystemInfo;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;

public class CaptorTool {
	
	static {
		if(SystemInfo.isWindows) {
			try {
				Files.copy(CaptorTool.class.getClassLoader().getResourceAsStream("lib/jpcap.dll"), Paths.get("./jpcap.dll"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Logger.getLogger(CaptorTool.class.getName()).log(Level.WARNING, e.getMessage(), e);
			}
		}else if(SystemInfo.isMacOS) {
			try {
				Files.copy(CaptorTool.class.getClassLoader().getResourceAsStream("lib/libjpcap.jnilib"), Paths.get("./libjpcap.jnilib"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Logger.getLogger(CaptorTool.class.getName()).log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	public static final boolean isJCaptorActive() {
		try {
			return JpcapCaptor.getDeviceList() != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static final synchronized Inet4Address[] filter_online_hosts(Inet4Address from, Inet4Address[] adrs, long timeout_ms) throws Exception {
		var iface = findNetworkInterfaceWithInet4Address(from).get();
		var cap = JpcapCaptor.openDevice(iface, 65535, true, 2000);
		cap.setNonBlockingMode(true);
		cap.setFilter("arp", true);
		Future<Inet4Address[]> future = Util.submit(new ARPReplyCollector(cap, timeout_ms));
		Stream.of(adrs).forEach(a -> sendARPRequest(cap, iface, a));
		adrs = future.get();
		cap.close();
		return adrs;
	}

	public static final Optional<NetworkInterface> findNetworkInterfaceWithInet4Address(Inet4Address adr) {
		return Stream.of(JpcapCaptor.getDeviceList()).filter(n -> Stream.of(n.addresses).map(a -> a.address).toList().contains(adr)).findFirst();
	}

	public static final void sendARPRequest(JpcapCaptor cap, NetworkInterface iface, Inet4Address adr) {
		ARPPacket pkt = new ARPPacket();
		pkt.hardtype = ARPPacket.HARDTYPE_ETHER;
		pkt.prototype = ARPPacket.PROTOTYPE_IP;
		pkt.hlen = 6;
		pkt.plen = 4;
		pkt.operation = ARPPacket.ARP_REQUEST;
		pkt.sender_hardaddr = iface.mac_address;
		pkt.sender_protoaddr = new byte[4];
		for (var a : iface.addresses) {
			if (a.address.getAddress().length == 4) {
				System.arraycopy(a.address.getAddress(), 0, pkt.sender_protoaddr, 0, 4);
				break;
			}
		}
		pkt.target_hardaddr = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		pkt.target_protoaddr = adr.getAddress();

		EthernetPacket ether = new EthernetPacket();
		ether.dst_mac = pkt.target_hardaddr;
		ether.src_mac = iface.mac_address;
		ether.frametype = EthernetPacket.ETHERTYPE_ARP;
		pkt.datalink = ether;
		var sender = cap.getJpcapSenderInstance();
		sender.sendPacket(pkt);
	}

	private static class ARPReplyCollector implements Callable<Inet4Address[]> {
		private final JpcapCaptor cap;
		private final long timeout_ms;
		private final List<Inet4Address> l = new LinkedList<>();

		public ARPReplyCollector(JpcapCaptor cap, long timeout_ms) {
			this.cap = cap;
			this.timeout_ms = timeout_ms;
		}

		@Override
		public Inet4Address[] call() throws Exception {
			long start_time = System.currentTimeMillis();
			while (System.currentTimeMillis() - start_time < timeout_ms) {
				ARPPacket pkt = (ARPPacket) cap.getPacket();
				if (pkt != null && pkt.operation == ARPPacket.ARP_REPLY) {
					Inet4Address adr = (Inet4Address) pkt.getSenderProtocolAddress();
					l.add(adr);
				}
				Thread.sleep(10);
			}
			return l.toArray(new Inet4Address[l.size()]);
		}

	}
}
