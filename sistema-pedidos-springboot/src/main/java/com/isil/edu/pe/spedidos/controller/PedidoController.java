package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador Spring MVC encargado de atender las solicitudes web
 * relacionadas con la gestión de pedidos.
 *
 * <p>Esta clase pertenece a la capa de presentación de la aplicación.
 * Su responsabilidad principal es recibir solicitudes HTTP,
 * obtener los datos enviados por el usuario, delegar la lógica de
 * negocio en {@link PedidoService} y seleccionar la vista que debe
 * mostrarse como respuesta.</p>
 *
 * <p>La anotación {@link Controller @Controller} indica a Spring que
 * esta clase debe registrarse como un componente de la capa web y que
 * sus métodos pueden participar en el procesamiento de solicitudes
 * HTTP mediante Spring MVC.</p>
 *
 * <p>La anotación:</p>
 *
 * <pre>
 * &#64;RequestMapping("/pedidos")
 * </pre>
 *
 * <p>define la ruta base atendida por este controlador. Por ejemplo:</p>
 *
 * <pre>
 * GET  /pedidos
 * POST /pedidos
 * </pre>
 *
 * <p>Dentro de la arquitectura de la aplicación, este controlador
 * ocupa la siguiente posición:</p>
 *
 * <pre>
 * Navegador
 *     |
 *     | HTTP
 *     v
 * DispatcherServlet
 *     |
 *     v
 * PedidoController
 *     |
 *     | Constructor Injection
 *     v
 * PedidoService
 *     |
 *     +----------------------+
 *     |                      |
 *     v                      v
 * PedidoRepository      InventarioClient
 *     |                      |
 *     v                      v
 *    H2              inventario-service
 * </pre>
 *
 * <p>Es importante observar que el controlador no contiene la lógica
 * principal para registrar pedidos ni conoce directamente cómo se
 * accede a la base de datos o cómo se consume el servicio de
 * Inventario. Estas responsabilidades se delegan a otras capas.</p>
 *
 * <p>La clase también traduce diferentes situaciones de la aplicación
 * a respuestas HTTP apropiadas:</p>
 *
 * <ul>
 *   <li>
 *     {@code 200 OK}: la operación se procesa normalmente.
 *   </li>
 *   <li>
 *     {@code 400 Bad Request}: existen datos ingresados incorrectamente.
 *   </li>
 *   <li>
 *     {@code 503 Service Unavailable}: el servicio externo de Inventario
 *     no se encuentra disponible.
 *   </li>
 *   <li>
 *     {@code 500 Internal Server Error}: ocurre un error interno no
 *     controlado específicamente.
 *   </li>
 * </ul>
 *
 * @see PedidoService
 * @see Pedido
 * @see Controller
 * @see RequestMapping
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

  private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

  /**
   * Servicio que contiene las operaciones de negocio relacionadas
   * con los pedidos.
   *
   * <p>El controlador depende de la interfaz {@link PedidoService}
   * y no de una implementación concreta, favoreciendo el desacoplamiento.</p>
   */
  private final PedidoService pedidoService;

  /**
   * Construye el controlador utilizando el servicio de pedidos
   * proporcionado por el contenedor IoC de Spring.
   *
   * <p>Este constructor representa un ejemplo de
   * <strong>Constructor Injection</strong>.</p>
   *
   * <p>El controlador no crea directamente el servicio utilizando:</p>
   *
   * <pre>
   * new PedidoServiceImpl(...)
   * </pre>
   *
   * <p>En cambio, declara la dependencia que necesita:</p>
   *
   * <pre>
   * PedidoController(PedidoService pedidoService)
   * </pre>
   *
   * <p>Spring localiza un bean que implemente {@link PedidoService}
   * y lo proporciona cuando crea el {@code PedidoController}.</p>
   *
   * <p>Conceptualmente:</p>
   *
   * <pre>
   * Spring IoC Container
   *         |
   *         v
   * PedidoServiceImpl
   *         |
   *         | inyección
   *         v
   * PedidoController
   * </pre>
   *
   * <p>Como la clase posee un único constructor, no es necesario
   * utilizar {@code @Autowired}.</p>
   *
   * @param pedidoService servicio utilizado para ejecutar las
   *                      operaciones de negocio de pedidos
   */
  public PedidoController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
  }

  /**
   * Atiende las solicitudes HTTP GET dirigidas a {@code /pedidos}
   * y prepara la información necesaria para mostrar la pantalla
   * principal de pedidos.
   *
   * <p>La anotación {@link GetMapping @GetMapping} vincula este
   * método con una solicitud:</p>
   *
   * <pre>
   * GET /pedidos
   * </pre>
   *
   * <p>El método carga en el {@link Model} la información requerida
   * por la vista, principalmente:</p>
   *
   * <ul>
   *   <li>Productos disponibles en Inventario.</li>
   *   <li>Pedidos registrados en el sistema.</li>
   * </ul>
   *
   * <p>Si la operación se realiza correctamente, retorna:</p>
   *
   * <pre>
   * "pedidos"
   * </pre>
   *
   * <p>Spring MVC interpreta este valor como el nombre lógico de
   * la vista que debe renderizarse.</p>
   *
   * <p>Por ejemplo, si la configuración contiene:</p>
   *
   * <pre>
   * spring.mvc.view.prefix=/WEB-INF/views/
   * spring.mvc.view.suffix=.jsp
   * </pre>
   *
   * <p>el nombre lógico {@code pedidos} puede resolverse como:</p>
   *
   * <pre>
   * /WEB-INF/views/pedidos.jsp
   * </pre>
   *
   * <p>Si {@code inventario-service} no se encuentra disponible,
   * la respuesta se transforma en un error HTTP
   * {@code 503 Service Unavailable}.</p>
   *
   * <p>Cualquier otro error de ejecución no tratado específicamente
   * se transforma en un error HTTP
   * {@code 500 Internal Server Error}.</p>
   *
   * @param model objeto utilizado para transportar información desde
   *              el Controller hacia la vista
   * @param response respuesta HTTP utilizada para establecer códigos
   *                 de estado cuando ocurre un error
   * @return nombre lógico de la vista que debe renderizar Spring MVC
   */
  @GetMapping
  public String listar(Model model, HttpServletResponse response) {
    try {
      cargarDatosVista(model);
      return "pedidos";
    } catch (InventarioNoDisponibleException e) {
      return mostrarServicioNoDisponible(model, response, e.getMessage());
    } catch (RuntimeException e) {
      log.error("Error inesperado al cargar la pantalla de pedidos.", e);
      return mostrarErrorGeneral(model, response);
    }
  }

  /**
   * Atiende las solicitudes HTTP POST utilizadas para registrar
   * un nuevo pedido.
   *
   * <p>La anotación {@link PostMapping @PostMapping} vincula este
   * método con:</p>
   *
   * <pre>
   * POST /pedidos
   * </pre>
   *
   * <p>Los parámetros enviados desde el formulario HTML son recibidos
   * mediante {@link RequestParam @RequestParam}:</p>
   *
   * <pre>
   * cliente
   * productoCodigo
   * cantidad
   * </pre>
   *
   * <p>La secuencia principal de procesamiento es:</p>
   *
   * <pre>
   * Formulario HTML
   *      |
   *      | POST /pedidos
   *      v
   * PedidoController.registrar(...)
   *      |
   *      | convierte cantidad String -> int
   *      v
   * PedidoService.registrarPedido(...)
   *      |
   *      v
   * Pedido registrado
   *      |
   *      | redirect
   *      v
   * GET /pedidos
   * </pre>
   *
   * <p>Después de registrar correctamente el pedido se utiliza el
   * patrón <strong>Post/Redirect/Get (PRG)</strong>.</p>
   *
   * <p>En lugar de devolver directamente la vista luego del POST,
   * se retorna:</p>
   *
   * <pre>
   * redirect:/pedidos
   * </pre>
   *
   * <p>Esto provoca una nueva solicitud GET y evita que al actualizar
   * el navegador se vuelva a enviar accidentalmente el formulario.</p>
   *
   * <p>Además, mediante {@link RedirectAttributes} se incorpora el
   * identificador del pedido creado como parámetro de la redirección.</p>
   *
   * <p>Ejemplo conceptual:</p>
   *
   * <pre>
   * POST /pedidos
   *
   *       |
   *       v
   *
   * Pedido creado: id = 15
   *
   *       |
   *       v
   *
   * redirect:/pedidos?creado=15
   *
   *       |
   *       v
   *
   * GET /pedidos?creado=15
   * </pre>
   *
   * <p>El método diferencia distintos tipos de error:</p>
   *
   * <ul>
   *   <li>
   *     {@link NumberFormatException}: la cantidad no puede convertirse
   *     a un número entero.
   *   </li>
   *   <li>
   *     {@link PedidoException}: se incumple una regla de negocio.
   *   </li>
   *   <li>
   *     {@link InventarioNoDisponibleException}: el servicio remoto
   *     de Inventario no está disponible.
   *   </li>
   *   <li>
   *     {@link RuntimeException}: ocurre un error interno no contemplado
   *     específicamente.
   *   </li>
   * </ul>
   *
   * @param cliente nombre del cliente enviado desde el formulario
   * @param productoCodigo código del producto seleccionado
   * @param cantidad cantidad solicitada recibida inicialmente como texto
   * @param model modelo utilizado para enviar información a la vista
   * @param response respuesta HTTP utilizada para establecer códigos
   *                 de estado
   * @param redirectAttributes atributos utilizados durante la redirección
   *                           posterior al registro exitoso
   * @return nombre lógico de la vista o instrucción de redirección
   */
  @PostMapping
  public String registrar(@RequestParam String cliente, @RequestParam String productoCodigo,
      @RequestParam String cantidad, Model model, HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    try {
      int cantidadNumerica = Integer.parseInt(cantidad);

      Pedido pedido = pedidoService.registrarPedido(cliente, productoCodigo, cantidadNumerica);

      // PRG: POST exitoso -> redirect -> GET.
      redirectAttributes.addAttribute("creado", pedido.getId());
      return "redirect:/pedidos";
    } catch (NumberFormatException e) {
      return mostrarErrorNegocio(model, response, cliente, productoCodigo, cantidad,
          "La cantidad ingresada es inválida.");
    } catch (InventarioNoDisponibleException e) {
      return mostrarServicioNoDisponible(model, response, e.getMessage());
    } catch (PedidoException e) {
      return mostrarErrorNegocio(model, response, cliente, productoCodigo, cantidad,
          e.getMessage());
    } catch (RuntimeException e) {
      log.error("Error inesperado al registrar un pedido.", e);
      return mostrarErrorGeneral(model, response);
    }
  }

  /**
   * Carga en el {@link Model} los datos requeridos para renderizar
   * la vista principal de pedidos.
   *
   * <p>Este método centraliza una operación que debe ejecutarse en
   * diferentes escenarios del Controller, evitando duplicar código.</p>
   *
   * <p>Se agregan dos atributos al modelo:</p>
   *
   * <pre>
   * "productos"
   * "pedidos"
   * </pre>
   *
   * <p>Posteriormente, una vista JSP puede acceder a ellos mediante
   * Expression Language:</p>
   *
   * <pre>
   * ${productos}
   * ${pedidos}
   * </pre>
   *
   * <p>El Controller no obtiene directamente los datos desde la base
   * de datos ni realiza llamadas HTTP hacia Inventario. Ambas
   * operaciones son delegadas a {@link PedidoService}.</p>
   *
   * @param model modelo al que se incorporarán los productos y pedidos
   * @throws InventarioNoDisponibleException si no es posible consultar
   *         los productos desde {@code inventario-service}
   */
  private void cargarDatosVista(Model model) {
    model.addAttribute("productos", pedidoService.listarProductos());
    model.addAttribute("pedidos", pedidoService.listarPedidos());
  }

  /**
   * Prepara la respuesta que debe mostrarse cuando la solicitud
   * contiene un error de negocio o de validación.
   *
   * <p>Este método establece el estado HTTP:</p>
   *
   * <pre>
   * 400 Bad Request
   * </pre>
   *
   * <p>El código {@code 400} indica que la solicitud no puede
   * procesarse correctamente debido a los datos proporcionados
   * por el cliente.</p>
   *
   * <p>Además, conserva los valores previamente ingresados por
   * el usuario para que puedan volver a mostrarse en el formulario:</p>
   *
   * <pre>
   * clienteIngresado
   * productoCodigoIngresado
   * cantidadIngresada
   * error
   * </pre>
   *
   * <p>Después intenta recargar los productos y pedidos necesarios
   * para volver a mostrar la vista {@code pedidos}.</p>
   *
   * <p>Si durante esa recarga el servicio de Inventario deja de estar
   * disponible, el flujo cambia a la vista general de error mediante
   * {@link #mostrarServicioNoDisponible(Model, HttpServletResponse, String)}.</p>
   *
   * @param model modelo utilizado para reenviar información a la vista
   * @param response respuesta HTTP cuyo código será establecido en 400
   * @param cliente valor del cliente previamente ingresado
   * @param productoCodigo código del producto previamente seleccionado
   * @param cantidad cantidad previamente ingresada
   * @param mensaje descripción del error que será mostrado al usuario
   * @return vista {@code pedidos} o vista de error si Inventario
   *         no se encuentra disponible
   */
  private String mostrarErrorNegocio(
      Model model,
      HttpServletResponse response,
      String cliente,
      String productoCodigo,
      String cantidad,
      String mensaje
  ) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());

    model.addAttribute("clienteIngresado", cliente);
    model.addAttribute("productoCodigoIngresado", productoCodigo);
    model.addAttribute("cantidadIngresada", cantidad);
    model.addAttribute("error", mensaje);

    try {
      cargarDatosVista(model);
      return "pedidos";
    } catch (InventarioNoDisponibleException e) {
      return mostrarServicioNoDisponible(model, response, e.getMessage());
    }
  }

  /**
   * Construye la respuesta utilizada cuando el servicio externo
   * de Inventario no se encuentra disponible.
   *
   * <p>Establece el estado HTTP:</p>
   *
   * <pre>
   * 503 Service Unavailable
   * </pre>
   *
   * <p>Este código es apropiado cuando la aplicación recibió la
   * solicitud correctamente, pero temporalmente no puede completar
   * la operación debido a la indisponibilidad de un servicio del
   * cual depende.</p>
   *
   * <p>En este proyecto puede ocurrir, por ejemplo, cuando:</p>
   *
   * <ul>
   *   <li>{@code inventario-service} está detenido.</li>
   *   <li>WildFly no está iniciado.</li>
   *   <li>El puerto de Inventario no responde.</li>
   *   <li>Se supera el timeout de conexión.</li>
   *   <li>Se supera el timeout de lectura.</li>
   * </ul>
   *
   * <p>El mensaje recibido se agrega al {@link Model} con la clave
   * {@code error} y posteriormente se renderiza la vista
   * {@code error}.</p>
   *
   * @param model modelo utilizado para transportar el mensaje de error
   * @param response respuesta HTTP cuyo estado será establecido en 503
   * @param mensaje descripción de la indisponibilidad del servicio
   * @return nombre lógico de la vista {@code error}
   */
  private String mostrarServicioNoDisponible(
      Model model,
      HttpServletResponse response,
      String mensaje
  ) {
    response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
    model.addAttribute("error", mensaje);
    return "error";
  }

  /**
   * Construye una respuesta genérica para errores internos que no
   * han sido tratados específicamente por el Controller.
   *
   * <p>El método establece el estado HTTP:</p>
   *
   * <pre>
   * 500 Internal Server Error
   * </pre>
   *
   * <p>El código {@code 500} representa una condición inesperada
   * ocurrida dentro de la aplicación mientras se procesaba la
   * solicitud.</p>
   *
   * <p>No se muestra directamente al usuario el mensaje técnico de
   * la excepción original. En su lugar, se proporciona un mensaje
   * genérico:</p>
   *
   * <pre>
   * Ocurrió un error interno al procesar la solicitud.
   * </pre>
   *
   * <p>Esta práctica evita exponer innecesariamente detalles internos
   * de implementación al cliente.</p>
   *
   * @param model modelo utilizado para transportar el mensaje de error
   * @param response respuesta HTTP cuyo estado será establecido en 500
   * @return nombre lógico de la vista {@code error}
   */
  private String mostrarErrorGeneral(Model model, HttpServletResponse response) {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    model.addAttribute("error", "Ocurrió un error interno al procesar la solicitud.");
    return "error";
  }

}
