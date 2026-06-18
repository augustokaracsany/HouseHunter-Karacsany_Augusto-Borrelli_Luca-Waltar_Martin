package Repository;

import org.mindrot.jbcrypt.BCrypt;

public interface Hashing {
    // Hashea. -> Cuando guardo. 
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    // Verifica. -> Cuando lo traigo de la BdD.
    public static boolean verificar(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}