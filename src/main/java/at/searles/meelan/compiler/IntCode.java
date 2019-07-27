package at.searles.meelan.compiler;

public class IntCode {
    private int[] array;
    private int offset;

    private int dataSize = 0;

    IntCode(int size) {
        this.array = new int[size];
        this.offset = 0;
    }

    public void add(int intCode) {
        array[offset++] = intCode;
    }

    public void add(int[] intCode) {
        System.arraycopy(intCode, 0, array, offset, intCode.length);
        offset += intCode.length;
    }

    public void updateDataSize(int regOffset, int regSize) {
        int sum = regOffset + regSize;

        if (sum > dataSize) {
            dataSize = sum;
        }
    }

    public int dataSize() {
        return dataSize;
    }

    public int[] createIntCode() {
        int[] code = new int[offset];
        System.arraycopy(array, 0, code, 0, offset);
        return code;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("const int code[] = {");

        boolean first = true;

        for (int codeword : array) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(codeword);
        }

        sb.append("}; // data size = ").append(dataSize)
                .append(", code size = ").append(array.length);

        sb.append("\nint codeLen = ").append(array.length).append(";");

        return sb.toString();
    }
}
