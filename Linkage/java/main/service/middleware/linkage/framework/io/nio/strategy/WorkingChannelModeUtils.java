package service.middleware.linkage.framework.io.nio.strategy;


/**
 * define the difference working mode for the read & write
 * @author zhonxu
 *
 */
public class WorkingChannelModeUtils {
	
	/**
	 * $MODESTR#$MESSAGEMODE$MODEEND#$;$STATSTR#$REQUEST$STATEND#$
	 * $MODESTR#$FILEMODE$MODEEND#$;$STATSTR#$REQUESTOK$STATEND#$
	 * @param receiveMessage
	 * @return
	 */
	public static boolean checkModeSwitch(String receiveMessage){
		return receiveMessage.contains("$MODESTR#$") && receiveMessage.contains("$MODEEND#$");
	}
	
	/**
	 * $MODESTR#$MESSAGEMODE$MODEEND#$
	 * $MODESTR#$FILEMODE$MODEEND#$
	 * @param receiveMessage
	 * @return
	 */
	public static WorkingChannelMode getModeSwitch(String receiveMessage){
		String modeString = receiveMessage.substring(receiveMessage.indexOf("$MODESTR#$") + "$MODESTR#$".length(), 
				receiveMessage.indexOf("$MODEEND#$"));
		return  WorkingChannelMode.valueOf(WorkingChannelMode.class, modeString);
	}
	
	/**
	 * $MODESTR#$MESSAGEMODE$MODEEND#$
	 * $MODESTR#$FILEMODE$MODEEND#$
	 * @param receiveMessage
	 * @return
	 */
	public static WorkingChannelModeSwitchState getModeSwitchState(String receiveMessage){
		String modeString = receiveMessage.substring(receiveMessage.indexOf("$STATSTR#$") + "$STATSTR#$".length(), 
				receiveMessage.indexOf("$STATEND#$"));
		return  WorkingChannelModeSwitchState.valueOf(WorkingChannelModeSwitchState.class, modeString);
	}
	
	/**
	 * $MODESTR#$MESSAGEMODE$MODEEND#$
	 * $MODESTR#$FILEMODE$MODEEND#$
	 * @param receiveMessage
	 * @return
	 */
	public static String getModeSwitchString(WorkingChannelMode workingChannelMode, WorkingChannelModeSwitchState workingChannelModeSwitchState){
		return "$MODESTR#$" + workingChannelMode.toString() + "$MODEEND#$;$STATSTR#$" + workingChannelModeSwitchState.toString() + "$STATEND#$";
	}
}
