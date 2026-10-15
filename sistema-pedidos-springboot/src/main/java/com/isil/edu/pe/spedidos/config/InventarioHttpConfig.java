package com.isil.edu.pe.spedidos.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Clase de configuración responsable de crear y registrar el cliente HTTP
 * utilizado por el Sistema de Pedidos para comunicarse con
 * {@code inventario-service}.
 *
 * <p>La anotación {@link Configuration @Configuration} indica que esta clase
 * contiene definiciones de objetos que serán administrados por el contenedor
 * IoC de Spring.</p>
 *
 * <p>En este caso, la clase crea un {@link RestClient} configurado con los
 * tiempos máximos de conexión y lectura definidos en
 * {@code application.properties}.</p>
 *
 * <p>El objetivo es evitar que las clases encargadas de consumir
 * {@code inventario-service} creen directamente su infraestructura HTTP
 * mediante {@code new}. En su lugar, Spring crea el cliente una sola vez
 * como un bean y posteriormente puede inyectarlo donde sea necesario.</p>
 *
 * <p>El flujo conceptual es:</p>
 *
 * <pre>
 * application.properties
 *        |
 *        | connect-timeout / read-timeout
 *        v
 * InventarioHttpConfig
 *        |
 *        | crea
 *        v
 * RestClient
 *        |
 *        | Spring registra el bean
 *        v
 * ApplicationContext
 *        |
 *        | inyección de dependencias
 *        v
 * InventarioRestClient
 *        |
 *        v
 * inventario-service
 * </pre>
 *
 * <p>Esta configuración permite separar dos responsabilidades:</p>
 *
 * <ul>
 *   <li>
 *       {@code InventarioHttpConfig}: configura la infraestructura HTTP.
 *   </li>
 *   <li>
 *       {@code InventarioRestClient}: utiliza esa infraestructura para
 *       ejecutar las operaciones del contrato {@code InventarioClient}.
 *   </li>
 * </ul>
 *
 * <p>Esta separación favorece el desacoplamiento, la reutilización de la
 * configuración y la sustitución de componentes durante las pruebas.</p>
 *
 * @see Configuration
 * @see Bean
 * @see RestClient
 */
@Configuration
public class InventarioHttpConfig {

  /**
   * Crea y registra el {@link RestClient} utilizado para las comunicaciones
   * HTTP con el servicio externo de Inventario.
   *
   * <p>La anotación {@link Bean @Bean} indica a Spring que el objeto
   * retornado por este método debe incorporarse al
   * {@code ApplicationContext} y quedar administrado por el contenedor IoC.</p>
   *
   * <p>El bean se registra con el nombre
   * {@code inventarioHttpClient}. Este nombre permite identificarlo
   * posteriormente mediante {@code @Qualifier} cuando existan varios
   * objetos {@link RestClient} dentro de la misma aplicación.</p>
   *
   * <p>Los valores de los parámetros son obtenidos desde
   * {@code application.properties} utilizando {@code @Value}.</p>
   *
   * <p>Por ejemplo:</p>
   *
   * <pre>
   * inventario.api.connect-timeout-ms=2000
   * inventario.api.read-timeout-ms=3000
   * </pre>
   *
   * <p>El timeout de conexión determina cuánto tiempo puede esperar la
   * aplicación mientras intenta establecer una conexión con
   * {@code inventario-service}.</p>
   *
   * <p>El timeout de lectura determina cuánto tiempo puede esperar la
   * aplicación por la respuesta una vez establecida la comunicación.</p>
   *
   * <p>Los valores predeterminados declarados en {@code @Value} permiten
   * que la aplicación tenga una configuración de respaldo en caso de que
   * las propiedades no hayan sido definidas explícitamente.</p>
   *
   * <p>Conceptualmente, Spring ejecuta este método durante la creación del
   * contexto de la aplicación:</p>
   *
   * <pre>
   * @Bean
   * inventarioHttpClient(...)
   *        |
   *        v
   * new RestClient configurado
   *        |
   *        v
   * ApplicationContext
   *        |
   *        v
   * @Qualifier("inventarioHttpClient")
   *        |
   *        v
   * InventarioRestClient
   * </pre>
   *
   * @param connectTimeout tiempo máximo, expresado en milisegundos,
   *                       permitido para establecer la conexión HTTP;
   *                       por defecto se utilizan {@code 2000 ms}
   *
   * @param readTimeout tiempo máximo, expresado en milisegundos,
   *                    permitido para esperar la respuesta del servicio;
   *                    por defecto se utilizan {@code 3000 ms}
   *
   * @return un {@link RestClient} configurado y administrado por Spring
   *         para consumir {@code inventario-service}
   */
  @Bean("inventarioHttpClient")
  public RestClient inventarioHttpClient(

      @Value("${inventario.api.connect-timeout-ms:2000}")
      long connectTimeout,

      @Value("${inventario.api.read-timeout-ms:3000}")
      long readTimeout) {

    /*
     * Define la fábrica encargada de crear las solicitudes HTTP.
     * Sobre ella configuramos los tiempos máximos de conexión
     * y lectura.
     */
    SimpleClientHttpRequestFactory factory =
        new SimpleClientHttpRequestFactory();

    /*
     * Tiempo máximo permitido para establecer la conexión
     * con inventario-service.
     */
    factory.setConnectTimeout(
        Duration.ofMillis(connectTimeout)
    );

    /*
     * Tiempo máximo permitido para esperar datos después de
     * haber establecido la conexión.
     */
    factory.setReadTimeout(
        Duration.ofMillis(readTimeout)
    );

    /*
     * Se construye el RestClient utilizando la configuración
     * HTTP definida anteriormente.
     *
     * Spring conservará el objeto retornado como un bean
     * administrado por su contenedor IoC.
     */
    return RestClient.builder()
        .requestFactory(factory)
        .build();
  }

}
