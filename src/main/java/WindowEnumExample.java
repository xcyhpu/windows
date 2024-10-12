import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.sun.jna.platform.win32.User32.HWND;

public class WindowEnumExample {



    public static void main(String[] args) throws Exception {

        INSTANCE = Native.load("user32", User32.class);

        HWND desktopWindow = INSTANCE.GetDesktopWindow();

        WinDef.RECT deskTopRec = INSTANCE.GetWindowRect(desktopWindow);
        // 1920 * 1080
        log("DesktopWindow", deskTopRec.toRectangle());
        // if(INSTANCE.SetActiveWindow(desktopWindow) == null) {
        // log("GetLastError", Kernel32.INSTANCE.GetLastError());
        // } else {
        // log("显示桌面", INSTANCE.SetForegroundWindow(desktopWindow));
        // }
        // 所有打开的应用程序窗口都是桌面窗口的子窗口
        // enumAllSubWindows(desktopWindow, null, WindowEnumExample::log);

        // 点击的位置的X、Y取窗口右上角往里一点，即关闭窗口按钮的位置
        // long x1 = 40;
        // long y1 = 40;

        /* for(long x1 = 40; x1 < 400; x1+=40 ) {
            for(long y1 = 40; y1 < 400; y1+=40 ) {
                // 低16位是x，高16位是y
                long position1 = (x1) + ((y1) << 16);
                log("ButtonDownDesktop", Long.toBinaryString(position1), "x", x1, "y", y1, INSTANCE.SendMessageA(desktopWindow, 0x0201, new WinDef.WPARAM(0x0001), new WinDef.LPARAM(position1)));
                log("ButtonUpDesktop", Long.toBinaryString(position1), "x", x1, "y", y1, INSTANCE.SendMessageA(desktopWindow, 0x0202, new WinDef.WPARAM(0x0001), new WinDef.LPARAM(position1)));
                Thread.sleep(500L);
                if(y1 == 360) {
                    LOG.flush();
                }
            }
        } */

        HWND createWindowA = com.sun.jna.platform.win32.User32.INSTANCE.CreateWindowEx(0x400, null, null, com.sun.jna.platform.win32.User32.WS_TILEDWINDOW,
                deskTopRec.left + 300, deskTopRec.top + 300, 400, 400,
                null, null, null, null);

//        HWND createWindowA = INSTANCE.CreateWindowW(null, null, new WinDef.DWORD(com.sun.jna.platform.win32.User32.WS_TILEDWINDOW),
//                deskTopRec.left + 300, deskTopRec.top + 300, 400, 400,
//                null, null, null, null);
        Thread.sleep(1000L);
        com.sun.jna.platform.win32.User32.INSTANCE.DestroyWindow(createWindowA);

        while (true) {

            TRACE_ID = "[" + System.currentTimeMillis() + "]";
            final List<HWND> WARNING_WINDOWS = new ArrayList<>();
            INSTANCE.EnumWindows((hWnd, pointer) -> {
                String title = INSTANCE.GetWindowTextW(hWnd);
                if (title.equals("Microsoft Word 安全声明")) {
                    WARNING_WINDOWS.add(hWnd);
                }
                return true;
            }, null);

            for (HWND warningWindow : WARNING_WINDOWS) {
                log("ClassName", INSTANCE.GetClassNameA(warningWindow));  // NUIDialog
//                log("窗口前置", INSTANCE.SetForegroundWindow(warningWindow));
//                log("窗口激活", INSTANCE.SetActiveWindow(warningWindow));

                // enumAllSubWindows(warningWindow, null, WindowEnumExample::log);

//                WindowInfo NetUICtrlNotifySink = enumAllSubWindows(warningWindow, hwnd -> INSTANCE.GetClassNameA(hwnd).equalsIgnoreCase("NetUICtrlNotifySink"), WindowEnumExample::log);
//                if(NetUICtrlNotifySink != null) {
//                    warningWindow = NetUICtrlNotifySink.getHwnd();
//                }
//                HWND NetUICtrlNotifySink = INSTANCE.FindWindowExA(warningWindow, null, "NetUICtrlNotifySink", null);
//                log("NetUICtrlNotifySink", NetUICtrlNotifySink);

//                long downParam = 1L;
//                long upParam = 0xC1000001L;
//                log("PostKeyDown: " + INSTANCE.PostMessageA(warningWindow, com.sun.jna.platform.win32.User32.WM_KEYDOWN, new WinDef.WPARAM(Win32VK.VK_Y.code), new WinDef.LPARAM(downParam)));
//                log("PostKeyUp: " + INSTANCE.PostMessageA(warningWindow, com.sun.jna.platform.win32.User32.WM_KEYUP, new WinDef.WPARAM(Win32VK.VK_Y.code), new WinDef.LPARAM(upParam)));
//                log("FindButton", INSTANCE.FindWindowExA(warningWindow, null, "Button", null));

//                // 鼠标点击事件
//                WinDef.RECT rect = INSTANCE.GetWindowRect(warningWindow);
////                // 点击的位置的X、Y取窗口右上角往里一点，即关闭窗口按钮的位置
//                // xy是相对父窗口的位置，所以应该在其宽高上调整
//                long x = rect.right - rect.left - 20;
//                long y = 20;
////                // 低16位是x，高16位是y
//                long position = (x) + ((y) << 16);
//                log("ButtonDown", INSTANCE.SendMessageA(warningWindow, 0x0201, new WinDef.WPARAM(0x0001), new WinDef.LPARAM(position)));
//                log("ButtonUp", INSTANCE.SendMessageA(warningWindow, 0x0202, new WinDef.WPARAM(0x0001), new WinDef.LPARAM(position)));

                // 直接关闭窗口，没选中"是(Y)"，应该发送一个Y按键消息
                log("SendCloseMessage: " + INSTANCE.SendMessageA(warningWindow, com.sun.jna.platform.win32.User32.WM_CLOSE, new WinDef.WPARAM(), new WinDef.LPARAM()));

                LOG.flush();
            }
            Thread.sleep(3000L);
        }
    }


    private static WindowInfo enumAllSubWindows(WinDef.HWND hwnd, Predicate<WinDef.HWND> predicate, Consumer<Object[]> logger) {
        return enumAllSubWindows(hwnd, 0, predicate, logger);
    }

    private static WindowInfo enumAllSubWindows(WinDef.HWND root, int indent, Predicate<WinDef.HWND> predicate, Consumer<Object[]> logger) {

        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("        ");
        }

        List<WinDef.HWND> subHandlers = new ArrayList<>();

        INSTANCE.EnumChildWindows(root, (hWnd, refData) -> {
            subHandlers.add(hWnd);
            return true; // 继续枚举
        }, null);

        logger.accept(new Object[]{indentStr + INSTANCE.GetClassNameA(root), subHandlers.size() + "个子窗口", INSTANCE.GetWindowRect(root).toRectangle()});

        if (subHandlers.isEmpty()) {
            return null;
        }

//        HWND target = subHandlers.stream().filter(a -> INSTANCE.GetClassNameA(a).equalsIgnoreCase("RICHEDIT60W")).findFirst().orElse(null);
        if(predicate != null) {
            HWND target = subHandlers.stream().filter(predicate).findFirst().orElse(null);
            if (target != null) {
                return new WindowInfo(target, INSTANCE.GetWindowRect(target));
            }
        }

        for (WinDef.HWND subHandler : subHandlers) {
            WindowInfo windowInfo = enumAllSubWindows(subHandler, indent + 1, predicate, logger);
            if (windowInfo != null) {
                return windowInfo;
            }
        }

        return null;
    }


    static class WindowInfo {

        private HWND hwnd;
        private WinDef.RECT rect;

        public WindowInfo(HWND hwnd, WinDef.RECT rect) {
            this.hwnd = hwnd;
            this.rect = rect;
        }

        public HWND getHwnd() {
            return hwnd;
        }

        public void setHwnd(HWND hwnd) {
            this.hwnd = hwnd;
        }

        public WinDef.RECT getRect() {
            return rect;
        }

        public void setRect(WinDef.RECT rect) {
            this.rect = rect;
        }
    }


    static User32 INSTANCE = null;

    public interface User32 extends StdCallLibrary {

        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
        boolean EnumChildWindows(WinDef.HWND hWndParent, WinUser.WNDENUMPROC lpEnumFunc, Pointer refData);
        int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
        boolean SetForegroundWindow(WinDef.HWND hWnd);
        int GetClassNameA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);
        WinDef.LRESULT SendMessageA(WinDef.HWND hWnd, int msg, WinDef.WPARAM wparam, WinDef.LPARAM lparam);
        WinDef.LRESULT PostMessageA(WinDef.HWND hWnd, int msg, WinDef.WPARAM wparam, WinDef.LPARAM lparam);
        WinDef.HWND FindWindowExA(WinDef.HWND hWnd, WinDef.HWND afterWhich, String className, String title);
        int GetWindowRect(HWND hWnd, WinDef.RECT rect);
        HWND CreateWindowW(WTypes.LPSTR lpClassName, WTypes.LPSTR lpWindowName, WinDef.DWORD dwStyle,
                           int x, int y, int nWidth, int nHeight,
                           HWND hWndParent, WinDef.HMENU hMenu, WinDef.HINSTANCE hInstance, WinDef.LPVOID lpParam);
        HWND GetDesktopWindow();
        HWND SetActiveWindow(HWND hWnd);


        default WinDef.RECT GetWindowRect(HWND hWnd) {
            WinDef.RECT rect = new WinDef.RECT();
            INSTANCE.GetWindowRect(hWnd, rect);
            return rect;
        }
        default String GetClassNameA(WinDef.HWND hWnd) {
            byte[] title = new byte[512];
            GetClassNameA(hWnd, title, title.length);
            return Native.toString(title);
        }
        default String GetWindowTextW(WinDef.HWND hWnd) {
            char[] title = new char[512];
            GetWindowTextW(hWnd, title, title.length);
            return Native.toString(title);
        }
    }



    private static String TRACE_ID = null;

    private static final BufferedWriter LOG;

    static {
        try {
            LOG = new BufferedWriter(new FileWriter("C:\\Users\\Administrator\\Desktop\\AutoClose.log", true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void log(Object... log) {

        String content = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + "\t" + TRACE_ID + "\t" + formatLog(log);

        try {
            LOG.write(content);
            LOG.newLine();
            System.out.println(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatLog(Object[] logs) {
        StringJoiner result = new StringJoiner(",");
        if (logs != null) {
            for (Object log : logs) {
                result.add("[" + log + "]");
            }
        }
        return result.toString();
    }
}
