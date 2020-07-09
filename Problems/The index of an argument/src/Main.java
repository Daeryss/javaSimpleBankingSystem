class Problem {
    public static void main(String[] args) {
        int x = -1;
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("test")) {
                x = i;
                break;
            }
        }
        System.out.println(x);
    }
}