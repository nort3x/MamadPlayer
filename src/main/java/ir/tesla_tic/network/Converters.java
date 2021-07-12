package ir.tesla_tic.network;

public class Converters {

    public static int bytesToUint16BigEndian(byte[] arr) {
        return ((arr[0] & 0xFF) << 8) |
                ((arr[1] & 0xFF));
    }

    public static byte[] uint16ToBytesBigEndian(int i) {
        return new byte[]{
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }

    public static long bytesToUint32BigEndian(byte[] arr) {
        return ((arr[0] & 0xFF) << 24) |
                ((arr[1] & 0xFF) << 16) |
                ((arr[2] & 0xFF) << 8) |
                ((arr[3] & 0xFF));
    }

    public static byte[] uint32ToBytesBigEndian(long l) {
        return new byte[]{
                (byte) ((l >> 24) & 0xFF),
                (byte) ((l >> 16) & 0xFF),
                (byte) ((l >> 8) & 0xFF),
                (byte) (l & 0xff)
        };
    }

    public static long bytesToUint64BigEndian(byte[] b) {
        return ((long) b[0] << 56)
                | ((long) b[1] & 0xff) << 48
                | ((long) b[2] & 0xff) << 40
                | ((long) b[3] & 0xff) << 32
                | ((long) b[4] & 0xff) << 24
                | ((long) b[5] & 0xff) << 16
                | ((long) b[6] & 0xff) << 8
                | ((long) b[7] & 0xff);
    }

    public static byte[] uint64ToBytesBigEndian(long l) {
        return new byte[]{
                (byte) ((l >> 56) & 0xFF),
                (byte) ((l >> 48) & 0xFF),
                (byte) ((l >> 40) & 0xFF),
                (byte) ((l >> 32) & 0xFF),
                (byte) ((l >> 24) & 0xFF),
                (byte) ((l >> 16) & 0xFF),
                (byte) ((l >> 8) & 0xFF),
                (byte) (l & 0xff)
        };
    }
}
