import db.DatabaseConnection;
import repositories.Address.AddressRepository;
import repositories.Address.JdbcAddressRepository;
import repositories.Category.CategoryRepository;
import repositories.Category.JdbcCategoryRepository;
import repositories.EmployeeDetails.EmployeeDetailsRepository;
import repositories.EmployeeDetails.JdbcEmployeeDetailsRepository;
import repositories.Ingredient.IngredientRepository;
import repositories.Ingredient.JdbcIngredientRepository;
import repositories.Order.JdbcOrderRepository;
import repositories.Order.OrderRepository;
import repositories.OrderStatusHistory.JdbcOrderStatusHistoryRepository;
import repositories.OrderStatusHistory.OrderStatusHistoryRepository;
import repositories.Payment.JdbcPaymentRepository;
import repositories.Payment.PaymentRepository;
import repositories.Product.JdbcProductRepository;
import repositories.Product.ProductRepository;
import repositories.Shift.JdbcShiftRepository;
import repositories.Shift.ShiftRepository;
import repositories.User.JdbcUserRepository;
import repositories.User.UserRepository;
import server.Server;
import services.*;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();

            // repositories
            UserRepository               userRepository               = new JdbcUserRepository(db);
            EmployeeDetailsRepository    employeeDetailsRepository    = new JdbcEmployeeDetailsRepository(db);
            ShiftRepository              shiftRepository              = new JdbcShiftRepository(db);
            AddressRepository            addressRepository            = new JdbcAddressRepository(db);
            CategoryRepository           categoryRepository           = new JdbcCategoryRepository(db);
            ProductRepository            productRepository            = new JdbcProductRepository(db);
            IngredientRepository         ingredientRepository         = new JdbcIngredientRepository(db);
            OrderRepository              orderRepository              = new JdbcOrderRepository(db);
            PaymentRepository            paymentRepository            = new JdbcPaymentRepository(db);
            OrderStatusHistoryRepository historyRepository            = new JdbcOrderStatusHistoryRepository(db);

            // services
            UserService         userService         = new UserService(userRepository);
            EmployeeService     employeeService     = new EmployeeService(
                    employeeDetailsRepository, shiftRepository, userRepository);
            AddressService      addressService      = new AddressService(addressRepository);
            ProductService      productService      = new ProductService(productRepository, categoryRepository);
            IngredientService   ingredientService   = new IngredientService(ingredientRepository, productService);
            PaymentService      paymentService      = new PaymentService(paymentRepository);
            OrderService        orderService        = new OrderService(orderRepository, ingredientService, paymentService);
            OrderHistoryService orderHistoryService = new OrderHistoryService(historyRepository);

            // container
            ServiceContainer services = new ServiceContainer(
                    userService, employeeService, addressService,
                    productService, orderService, ingredientService,
                    paymentService, orderHistoryService);

            new Server(services).start();

        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}