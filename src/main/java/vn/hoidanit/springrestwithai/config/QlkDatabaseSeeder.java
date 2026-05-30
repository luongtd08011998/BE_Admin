package vn.hoidanit.springrestwithai.config;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.qlk.category.QlkCategory;
import vn.hoidanit.springrestwithai.qlk.category.QlkCategoryRepository;
import vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseRepository;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseStatus;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseUser;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseUserRepository;

@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class QlkDatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(QlkDatabaseSeeder.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final QlkCategoryRepository qlkCategoryRepository;
    private final WarehouseUserRepository warehouseUserRepository;

    public QlkDatabaseSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            WarehouseRepository warehouseRepository,
            QlkCategoryRepository qlkCategoryRepository,
            WarehouseUserRepository warehouseUserRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.qlkCategoryRepository = qlkCategoryRepository;
        this.warehouseUserRepository = warehouseUserRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (warehouseRepository.count() > 0) {
            log.info(">>> QLK Database already seeded — skipping");
            return;
        }

        log.info(">>> Seeding QLK database...");

        List<Permission> qlkPermissions = seedQlkPermissions();
        seedQlkRoles(qlkPermissions);
        List<Warehouse> warehouses = seedWarehouses();
        
        assignUsersToWarehouses(warehouses);

        log.info(">>> QLK Database seeded successfully");
    }

    private List<Permission> seedQlkPermissions() {
        List<Permission> permissions = List.of(
                // WAREHOUSE
                createPermission("CREATE_WAREHOUSE", "/api/v1/qlk/warehouses", "POST", "QLK_WAREHOUSE"),
                createPermission("UPDATE_WAREHOUSE", "/api/v1/qlk/warehouses/{id}", "PUT", "QLK_WAREHOUSE"),
                createPermission("DELETE_WAREHOUSE", "/api/v1/qlk/warehouses/{id}", "DELETE", "QLK_WAREHOUSE"),
                createPermission("VIEW_WAREHOUSES", "/api/v1/qlk/warehouses", "GET", "QLK_WAREHOUSE"),
                createPermission("VIEW_WAREHOUSE", "/api/v1/qlk/warehouses/{id}", "GET", "QLK_WAREHOUSE"),
                createPermission("ASSIGN_WAREHOUSE_USERS", "/api/v1/qlk/warehouses/{id}/users", "POST", "QLK_WAREHOUSE"),
                createPermission("VIEW_WAREHOUSE_USERS", "/api/v1/qlk/warehouses/{id}/users", "GET", "QLK_WAREHOUSE"),

                // CATEGORY
                createPermission("CREATE_QLK_CATEGORY", "/api/v1/qlk/categories", "POST", "QLK_CATEGORY"),
                createPermission("UPDATE_QLK_CATEGORY", "/api/v1/qlk/categories/{id}", "PUT", "QLK_CATEGORY"),
                createPermission("DELETE_QLK_CATEGORY", "/api/v1/qlk/categories/{id}", "DELETE", "QLK_CATEGORY"),
                createPermission("VIEW_QLK_CATEGORIES", "/api/v1/qlk/categories", "GET", "QLK_CATEGORY"),
                createPermission("VIEW_QLK_CATEGORY", "/api/v1/qlk/categories/{id}", "GET", "QLK_CATEGORY"),

                // SUPPLIER
                createPermission("CREATE_SUPPLIER", "/api/v1/qlk/suppliers", "POST", "QLK_SUPPLIER"),
                createPermission("UPDATE_SUPPLIER", "/api/v1/qlk/suppliers/{id}", "PUT", "QLK_SUPPLIER"),
                createPermission("DELETE_SUPPLIER", "/api/v1/qlk/suppliers/{id}", "DELETE", "QLK_SUPPLIER"),
                createPermission("VIEW_SUPPLIERS", "/api/v1/qlk/suppliers", "GET", "QLK_SUPPLIER"),
                createPermission("VIEW_SUPPLIER", "/api/v1/qlk/suppliers/{id}", "GET", "QLK_SUPPLIER"),

                // MATERIAL
                createPermission("CREATE_MATERIAL", "/api/v1/qlk/materials", "POST", "QLK_MATERIAL"),
                createPermission("UPDATE_MATERIAL", "/api/v1/qlk/materials/{id}", "PUT", "QLK_MATERIAL"),
                createPermission("DELETE_MATERIAL", "/api/v1/qlk/materials/{id}", "DELETE", "QLK_MATERIAL"),
                createPermission("VIEW_MATERIALS", "/api/v1/qlk/materials", "GET", "QLK_MATERIAL"),
                createPermission("VIEW_MATERIAL", "/api/v1/qlk/materials/{id}", "GET", "QLK_MATERIAL"),

                // STOCK VOUCHER
                createPermission("CREATE_STOCK_VOUCHER", "/api/v1/qlk/warehouses/{warehouseId}/vouchers", "POST", "QLK_STOCK_VOUCHER"),
                createPermission("UPDATE_STOCK_VOUCHER", "/api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}", "PUT", "QLK_STOCK_VOUCHER"),
                createPermission("DELETE_STOCK_VOUCHER", "/api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}", "DELETE", "QLK_STOCK_VOUCHER"),
                createPermission("VIEW_STOCK_VOUCHERS", "/api/v1/qlk/warehouses/{warehouseId}/vouchers", "GET", "QLK_STOCK_VOUCHER"),
                createPermission("VIEW_STOCK_VOUCHER", "/api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}", "GET", "QLK_STOCK_VOUCHER"),
                createPermission("SUBMIT_STOCK_VOUCHER", "/api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}/submit", "POST", "QLK_STOCK_VOUCHER"),
                createPermission("APPROVE_STOCK_VOUCHER", "/api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}/approve", "POST", "QLK_STOCK_VOUCHER"),

                // INVENTORY
                createPermission("VIEW_INVENTORY", "/api/v1/qlk/warehouses/{warehouseId}/inventory/stocks", "GET", "QLK_INVENTORY"),
                createPermission("VIEW_INVENTORY_TX", "/api/v1/qlk/warehouses/{warehouseId}/inventory/transactions", "GET", "QLK_INVENTORY"),
                createPermission("VIEW_INVENTORY_SNAPSHOT", "/api/v1/qlk/warehouses/{warehouseId}/inventory/snapshots", "GET", "QLK_INVENTORY")
        );

        return permissionRepository.saveAll(permissions);
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        return permission;
    }

    private void seedQlkRoles(List<Permission> qlkPermissions) {
        // SUPER_ADMIN gets everything, we should append to existing SUPER_ADMIN
        Optional<Role> superAdminOpt = roleRepository.findByName("SUPER_ADMIN");
        if (superAdminOpt.isPresent()) {
            Role superAdmin = superAdminOpt.get();
            superAdmin.getPermissions().addAll(qlkPermissions);
            roleRepository.save(superAdmin);
        }

        // THU_KHO (Warehouse Keeper)
        List<Permission> thuKhoPerms = filterPermissions(qlkPermissions,
                "VIEW_WAREHOUSES", "VIEW_WAREHOUSE",
                "VIEW_QLK_CATEGORIES", "VIEW_QLK_CATEGORY",
                "VIEW_SUPPLIERS", "VIEW_SUPPLIER",
                "VIEW_MATERIALS", "VIEW_MATERIAL",
                "CREATE_STOCK_VOUCHER", "UPDATE_STOCK_VOUCHER", "VIEW_STOCK_VOUCHERS", "VIEW_STOCK_VOUCHER", "SUBMIT_STOCK_VOUCHER",
                "VIEW_INVENTORY", "VIEW_INVENTORY_TX"
        );
        createRole("THU_KHO", "Thủ kho", thuKhoPerms);

        // KY_THUAT (Technician) - can only view inventory to request materials
        List<Permission> kyThuatPerms = filterPermissions(qlkPermissions,
                "VIEW_WAREHOUSES", "VIEW_WAREHOUSE",
                "VIEW_QLK_CATEGORIES", "VIEW_QLK_CATEGORY",
                "VIEW_MATERIALS", "VIEW_MATERIAL",
                "VIEW_INVENTORY"
        );
        createRole("KY_THUAT", "Kỹ thuật viên", kyThuatPerms);

        // GIAM_DOC (Director) - view all, approve vouchers
        List<Permission> giamDocPerms = filterPermissions(qlkPermissions,
                "VIEW_WAREHOUSES", "VIEW_WAREHOUSE",
                "VIEW_QLK_CATEGORIES", "VIEW_QLK_CATEGORY",
                "VIEW_SUPPLIERS", "VIEW_SUPPLIER",
                "VIEW_MATERIALS", "VIEW_MATERIAL",
                "VIEW_STOCK_VOUCHERS", "VIEW_STOCK_VOUCHER", "APPROVE_STOCK_VOUCHER",
                "VIEW_INVENTORY", "VIEW_INVENTORY_TX", "VIEW_INVENTORY_SNAPSHOT"
        );
        createRole("GIAM_DOC", "Giám đốc", giamDocPerms);
    }

    private Role createRole(String name, String description, List<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private List<Permission> filterPermissions(List<Permission> all, String... names) {
        List<String> nameList = List.of(names);
        return all.stream()
                .filter(p -> nameList.contains(p.getName()))
                .toList();
    }

    private List<Warehouse> seedWarehouses() {
        Warehouse it = Warehouse.builder().code("IT").name("Kho IT").status(WarehouseStatus.HOAT_DONG).build();
        Warehouse vanHanh = Warehouse.builder().code("VH").name("Kho Vận Hành").status(WarehouseStatus.HOAT_DONG).build();
        Warehouse baoTri = Warehouse.builder().code("BT").name("Kho Bảo trì").status(WarehouseStatus.HOAT_DONG).build();
        Warehouse hoaNghiem = Warehouse.builder().code("HN").name("Kho Hóa Nghiệm").status(WarehouseStatus.HOAT_DONG).build();

        return warehouseRepository.saveAll(List.of(it, vanHanh, baoTri, hoaNghiem));
    }

   

    private void assignUsersToWarehouses(List<Warehouse> warehouses) {
        // Find existing users
        Optional<User> superAdmin = userRepository.findByEmail("luongtd@toctienltd.vn");
        Optional<User> hrManager = userRepository.findByEmail("hr@toctienltd.vn");

        // We can assign luongtd to all warehouses if needed, though SUPER_ADMIN bypasses warehouse check anyway.
        if (superAdmin.isPresent()) {
            User adminUser = superAdmin.get();
            for (Warehouse wh : warehouses) {
                WarehouseUser wu = WarehouseUser.builder()
                        .warehouse(wh)
                        .user(adminUser)
                        .build();
                warehouseUserRepository.save(wu);
            }
        }
    }
}
