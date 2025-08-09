import gameVisualisation.MainGame;

import java.util.Scanner;

public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    System.out.println("What is the IP address of the game you are connecting to?");
    String ip_address = sc.nextLine();
    System.out.println();
    new MainGame(ip_address);
}
