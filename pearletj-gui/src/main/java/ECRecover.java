import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

public class ECRecover {

	private void pkDisplay() {
		//0x627306090abaB3A6e1400e9345bC60c78a8BEf57
		String privateKey1 = "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3";
		Credentials credentials = Credentials.create(privateKey1);
		System.out.println("pk:" + Numeric.toHexStringNoPrefix(credentials.getEcKeyPair().getPublicKey()));

		String message = "now that's a text";
		String label = "\u0019Ethereum Signed Message:\n"+ String.valueOf(message.getBytes().length) + message;
		System.out.println("hash:" + Hash.sha3String(label));

		ByteBuffer buffer = ByteBuffer.allocate(label.getBytes().length);
		buffer.put(label.getBytes());
		byte[] array = buffer.array();
		Sign.SignatureData signature = Sign.signMessage(array, credentials.getEcKeyPair(), true);

		ByteBuffer sigBuffer = ByteBuffer.allocate(signature.getR().length + signature.getS().length + 1);
		sigBuffer.put(signature.getR());
		sigBuffer.put(signature.getS());
		sigBuffer.put(signature.getV());
		System.out.println("sig:" + Numeric.toHexString(sigBuffer.array()));

		ECDSASignature esig = new ECDSASignature(Numeric.toBigInt(signature.getR()), Numeric.toBigInt(signature.getS()));
		BigInteger res = Sign.recoverFromSignature(0, esig, Hash.sha3(label.getBytes()));
		System.out.println("public Ethereum address: 0x" + Keys.getAddress(res));
	}
	public static void main(String[] args) {
		new ECRecover().pkDisplay();
	}

}