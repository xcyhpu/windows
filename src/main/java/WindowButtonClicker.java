import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

/**
 * @author :   xuchunyang
 * @version :   v1.0
 * @date :   2024-09-29 09:42
 */
public class WindowButtonClicker {

    private static final User32Extended INSTANCE = Native.load("user32", User32Extended.class);

    public interface User32Extended extends User32 {

        HWND FindWindow(String lpClassName, String lpWindowName);

        HWND FindWindowEx(HWND parent, HWND child, String className, String windowName);

//        LRESULT SendMessage(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);

//        boolean SendMessage(HWND hWnd, int Msg, Integer wParam, Integer lParam);
    }


    public static void main(String[] args) {
        String windowTitle = "Microsoft Word 安全声明";
        String buttonText = "否";

        WinDef.HWND hWnd = INSTANCE.FindWindow(null, windowTitle);
        if (hWnd == null) {
            System.out.println("Window not found");
            return;
        }

        WinDef.HWND hButton = INSTANCE.FindWindowEx(hWnd, null, null, buttonText);
        if (hButton == null) {
            System.out.println("Button not found");
            return;
        }

//        INSTANCE.SendMessage(hButton, WinUser.WM_LBUTTONDOWN, 0, 0);
//        INSTANCE.SendMessage(hButton, WinUser.WM_LBUTTONUP, 0, 0);

        System.out.println("PUSH BUTTON");
        System.out.println(INSTANCE.SendMessage(hButton, WinUser.BS_PUSHBUTTON, new WinDef.WPARAM(0), new WinDef.LPARAM(0)));
    }
}
