package ie.diyar_moein_ca5.controllers.Response;

public class JWTTokenResponse {
    private String token;
    private String username;
    private Integer code;
    private String messgae;

    public JWTTokenResponse(String token , String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername(){
        return username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessgae() {
        return messgae;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessgae(String messgae) {
        this.messgae = messgae;
    }
}
