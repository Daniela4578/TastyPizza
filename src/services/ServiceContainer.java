package services;

public class ServiceContainer {

    private final UserService       userService;
    private final EmployeeService   employeeService;
    private final AddressService    addressService;
    private final ProductService    productService;
    private final OrderService      orderService;
    private final IngredientService ingredientService;

    public ServiceContainer(UserService       userService,
                            EmployeeService   employeeService,
                            AddressService    addressService,
                            ProductService    productService,
                            OrderService      orderService,
                            IngredientService ingredientService) {
        this.userService       = userService;
        this.employeeService   = employeeService;
        this.addressService    = addressService;
        this.productService    = productService;
        this.orderService      = orderService;
        this.ingredientService = ingredientService;
    }

    public UserService       getUserService()       { return userService; }
    public EmployeeService   getEmployeeService()   { return employeeService; }
    public AddressService    getAddressService()    { return addressService; }
    public ProductService    getProductService()    { return productService; }
    public OrderService      getOrderService()      { return orderService; }
    public IngredientService getIngredientService() { return ingredientService; }
}