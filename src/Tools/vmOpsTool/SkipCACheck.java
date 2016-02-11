import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static javax.net.ssl.HttpsURLConnection.*;

public class SkipCACheck {

    public static void AllowUntrustedConnections() throws NoSuchAlgorithmException, KeyManagementException {
        System.out.println("Setting allow untrusted connections for the session.");

        HostnameVerifier verifier = (urlHostName, session) -> true;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager trustManager = new TrustAllTrustManager();
        trustAllCerts[0] = trustManager;

        SSLContext sslContext = SSLContext.getInstance("SSL");
        SSLSessionContext serverSessionContext = sslContext.getServerSessionContext();
        serverSessionContext.setSessionTimeout(30 * 60);
        sslContext.init(null, trustAllCerts, null);
        setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        setDefaultHostnameVerifier(verifier);
    }

    private static class TrustAllTrustManager implements TrustManager, X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
        }
    }
}
