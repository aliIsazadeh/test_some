import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

public class NewFolder {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int j = Integer.parseInt(scanner.nextLine());
        List<String> list = new ArrayList<>();
        String str;
        for (int i = 0; i < j; i++) {

            str = scanner.nextLine();
            if (list.contains(str))
                str = addNumAndGet(str,list,1);
            list.add(str);
            list.sort(String::compareTo);
            if (!list.isEmpty())
            printList(list);
        }
    }
    private static String addNumAndGet(String node,List<String> list,int depth){
        if (list.contains(depth==0?node:node+" ("+depth+")"))
            node = addNumAndGet(node,list,depth+1);
        else
            node = node+" ("+depth+")";

        return node;
    }
    private static void printList(List<String> list){
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (String s : list) {
            stringJoiner.add("'" +s+ "'");
        }
        System.out.println(stringJoiner.toString());
    }
}
