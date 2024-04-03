package ece568.amazon;

public interface UserInfoListener {
    /**
     * notify when user ID is received
     * 
     * @param userName
     */
    void onUserInfo(String userInfo);
}
