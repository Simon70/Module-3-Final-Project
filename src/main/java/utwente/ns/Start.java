package utwente.ns;

import org.reflections.Reflections;
import utwente.ns.applications.GUIChat;
import utwente.ns.applications.IApplication;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

/**
 * @author rhbvkleef
 *         Created on 4/10/17
 */
public class Start {

    public static final Class<? extends IApplication> AUTOSTART = GUIChat.class;

    public static void main(String[] args) {
        if (AUTOSTART != null) {
            startApplicationByClass(AUTOSTART);
            return;
        }

        Reflections applications = new Reflections("utwente.ns.applications");
        List<Class<? extends IApplication>> applicationClasses = new ArrayList<>(applications.getSubTypesOf(IApplication.class));
        int n;
        if (args.length == 1) {
            n = Integer.parseInt(args[0]);
            return;
        } else {
            
            System.out.println("Select the program thy wishes to allow to interfere with your life and take some of it away.\n");
            
            for (int i = 0; i < applicationClasses.size(); i++) {
                System.out.printf("%2d: %s\n", i, applicationClasses.get(i).getSimpleName());
            }
            
            System.out.printf("\nThou shallth enter a number in tween of %d and %d, excluding %d: ", 0, applicationClasses.size(), applicationClasses.size());
            
            n = new Scanner(System.in).nextInt();
            if (n >= applicationClasses.size()) {
                System.out.println(Util.figlet(new String(Base64.getDecoder().decode("ZmlnbGV0IC13IDcwIFRob3Ugc2hhbGx0aCBiZSB0ZXJtaW5hdGVk"))));
                return;
            }
        }
        
        Class<? extends IApplication> application = applicationClasses.get(n);
        
        startApplicationByClass(application);
    }

    private static void startApplicationByClass(Class<? extends IApplication> clazz) {
        try {
            clazz.newInstance().start();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
