import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class Win32FrontWindowHandle {
    // 接口User32定义了获取前台窗口句柄的函数
    public interface User32Extends extends User32 {
        HWND GetForegroundWindow();
    }

    // 获取当前活动窗口的句柄
    public static HWND getForegroundWindowHandle() {
        User32Extends user32 = Native.load("user32", User32Extends.class);
        return user32.GetForegroundWindow();
    }

    public static void main(String[] args) throws InterruptedException {
        while(true) {
            HWND foregroundWindowHandle = getForegroundWindowHandle();
            if(foregroundWindowHandle != null) {
                System.out.println("前台窗口句柄: " + foregroundWindowHandle);
            }
            Thread.sleep(2000L);
        }
    }
}