package pe.edu.isil.inventario.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activa Jakarta REST y define el DataSource utilizado por Jakarta Persistence.
 *
 * <p>La credencial incluida es únicamente para fines académicos. En producción debe
 * externalizarse en el servidor de aplicaciones/Credential Store.</p>
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
