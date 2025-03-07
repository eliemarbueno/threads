package principal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;

public class SharedMapWithUserContext implements Runnable {
 
    public static Map<Integer, String> userContextPerUserId
      = new ConcurrentHashMap<>();
    private Integer userId;

    @Override
    public void run() {
    	userContextPerUserId.put(userId, "user " + userId);
    }

}