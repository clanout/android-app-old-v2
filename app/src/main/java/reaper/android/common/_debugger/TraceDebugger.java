package reaper.android.common._debugger;

import timber.log.Timber;

public class TraceDebugger
{
    public static void print(String tag)
    {
        Timber.v(">>>> [%s] START", tag);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements)
        {
            String className = stackTraceElement.getClassName();

            if (className.contains("reaper") && !className.contains("TraceDebugger"))
            {
                Timber.v(">>>> [%s] %s.%s() : %s", tag, className,
                        stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
            }
        }
        Timber.v(">>>> [%s] END", tag);
    }
}
