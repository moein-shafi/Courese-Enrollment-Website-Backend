package ie.diyar_moein_ca5.Services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Calendar;
import java.util.Date;

public class JWTAuthentication {
    public static Algorithm algorithm = Algorithm.HMAC256("bolbolestan");

    public static String  createAndSignToken(String id) {
        Date expire = new Date();
        Calendar calender = Calendar.getInstance();
        calender.setTime(expire);
        calender.add(Calendar.HOUR_OF_DAY, 2); /// TODO: check this.
        expire = calender.getTime();

        return JWT.create()
                .withIssuer("bolbolestan.ir")
                .withIssuedAt(new Date())
                .withExpiresAt(expire)
                .withClaim("id", id)
                .sign(algorithm);
    }

}
