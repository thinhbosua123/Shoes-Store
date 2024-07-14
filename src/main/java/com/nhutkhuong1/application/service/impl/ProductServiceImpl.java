package com.nhutkhuong1.application.service.impl;

import com.nhutkhuong1.application.config.Contant;
import com.nhutkhuong1.application.entity.Product;
import com.nhutkhuong1.application.entity.ProductSize;
import com.nhutkhuong1.application.entity.Promotion;
import com.nhutkhuong1.application.exception.BadRequestException;
import com.nhutkhuong1.application.model.dto.DetailProductInfoDTO;
import com.nhutkhuong1.application.model.dto.PageableDTO;
import com.nhutkhuong1.application.model.dto.ProductInfoDTO;
import com.nhutkhuong1.application.model.dto.ShortProductInfoDTO;
import com.nhutkhuong1.application.model.mapper.ProductMapper;
import com.nhutkhuong1.application.model.request.CreateProductRequest;
import com.nhutkhuong1.application.model.request.CreateSizeCountRequest;
import com.nhutkhuong1.application.model.request.FilterProductRequest;
import com.nhutkhuong1.application.model.request.UpdateFeedBackRequest;
import com.nhutkhuong1.application.repository.OrderRepository;
import com.nhutkhuong1.application.repository.ProductRepository;
import com.nhutkhuong1.application.repository.ProductSizeRepository;
import com.nhutkhuong1.application.repository.PromotionRepository;
import com.nhutkhuong1.application.service.ProductService;
import com.nhutkhuong1.application.service.PromotionService;
import com.nhutkhuong1.application.utils.PageUtil;
import com.nhutkhuong1.application.exception.InternalServerException;
import com.nhutkhuong1.application.exception.NotFoundException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Page<Product> adminGetListProduct(String id, String name, String category, String brand, Integer page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, Contant.LIMIT_PRODUCT, Sort.by("created_at").descending());
        return productRepository.adminGetListProducts(id, name, category, brand, pageable);
    }

    @Override
    public Product createProduct(CreateProductRequest createProductRequest) {
        //Kiểm tra có danh muc
        if (createProductRequest.getCategoryIds().isEmpty()) {
            throw new BadRequestException("Danh mục trống!");
        }
        //Kiểm tra có ảnh sản phẩm
        if (createProductRequest.getImages().isEmpty()) {
            throw new BadRequestException("Ảnh sản phẩm trống!");
        }
        //Kiểm tra tên sản phẩm trùng
        Product product = productRepository.findByName(createProductRequest.getName());
        if (product != null) {
            throw new BadRequestException("Tên sản phẩm đã tồn tại trong hệ thống, Vui lòng chọn tên khác!");
        }

        product = ProductMapper.toProduct(createProductRequest);
        //Sinh id
        String id = RandomStringUtils.randomAlphanumeric(6);
        product.setId(id);
        product.setTotalSold(0);
        product.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        try {
            productRepository.save(product);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi thêm sản phẩm");
        }
        return product;
    }

    @Override
    public void updateProduct(CreateProductRequest createProductRequest, String id) {
        //Kiểm tra sản phẩm có tồn tại
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm!");
        }

        //Kiểm tra tên sản phẩm có tồn tại
        Product rs = productRepository.findByName(createProductRequest.getName());
        if (rs != null) {
            if (!createProductRequest.getId().equals(rs.getId()))
                throw new BadRequestException("Tên sản phẩm đã tồn tại trong hệ thống, Vui lòng chọn tên khác!");
        }

        //Kiểm tra có danh muc
        if (createProductRequest.getCategoryIds().isEmpty()) {
            throw new BadRequestException("Danh mục trống!");
        }

        //Kiểm tra có ảnh sản phẩm
        if (createProductRequest.getImages().isEmpty()) {
            throw new BadRequestException("Ảnh sản phẩm trống!");
        }

        Product result = ProductMapper.toProduct(createProductRequest);
        result.setId(id);
        result.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        try {
            productRepository.save(result);
        } catch (Exception e) {
            throw new InternalServerException("Có lỗi khi sửa sản phẩm!");
        }
    }

    @Override
    public Product getProductById(String id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm trong hệ thống!");
        }
        return product.get();
    }

    @Override
    public void deleteProduct(String[] ids) {
        for (String id : ids) {
            productRepository.deleteById(id);
        }
    }

    @Override
    public void deleteProductById(String id) {
        // Check product exist
        Optional<Product> rs = productRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }

        // If have order, can't delete
        int countOrder = orderRepository.countByProductId(id);
        if (countOrder > 0) {
            throw new BadRequestException("Sản phẩm đã được đặt hàng không thể xóa");
        }

        try {
            // Delete product size
            productSizeRepository.deleteByProductId(id);

            productRepository.deleteById(id);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new InternalServerException("Lỗi khi xóa sản phẩm");
        }
    }

    @Override
    public List<ProductInfoDTO> getListBestSellProducts() {
        List<ProductInfoDTO> productInfoDTOS = productRepository.getListBestSellProducts(Contant.LIMIT_PRODUCT_SELL);
        return checkPublicPromotion(productInfoDTOS);
    }

    @Override
    public List<ProductInfoDTO> getListNewProducts() {
        List<ProductInfoDTO> productInfoDTOS = productRepository.getListNewProducts(Contant.LIMIT_PRODUCT_NEW);
        return checkPublicPromotion(productInfoDTOS);

    }

    @Override
    public List<ProductInfoDTO> getListViewProducts() {
        List<ProductInfoDTO> productInfoDTOS = productRepository.getListViewProducts(Contant.LIMIT_PRODUCT_VIEW);
        return checkPublicPromotion(productInfoDTOS);
    }

    @Override
    public DetailProductInfoDTO getDetailProductById(String id) {
        Optional<Product> rs = productRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }
        Product product = rs.get();

        if (product.getStatus() != 1) {
            throw new NotFoundException("Sản phâm không tồn tại");
        }
        DetailProductInfoDTO dto = new DetailProductInfoDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getSalePrice());
        dto.setViews(product.getView());
        dto.setSlug(product.getSlug());
        dto.setTotalSold(product.getTotalSold());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setFeedbackImages(product.getImageFeedBack());
        dto.setProductImages(product.getImages());
        dto.setComments(product.getComments());

        //Cộng sản phẩm xem
        product.setView(product.getView() + 1);
        productRepository.save(product);

        //Kiểm tra có khuyến mại
        Promotion promotion = promotionService.checkPublicPromotion();
        if (promotion != null) {
            dto.setCouponCode(promotion.getCouponCode());
            dto.setPromotionPrice(promotionService.calculatePromotionPrice(dto.getPrice(), promotion));
        } else {
            dto.setCouponCode("");
        }
        return dto;

    }

    @Override
    public List<ProductInfoDTO> getRelatedProducts(String id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }
        List<ProductInfoDTO> products = productRepository.getRelatedProducts(id, Contant.LIMIT_PRODUCT_RELATED);
        return checkPublicPromotion(products);
    }

    @Override
    public List<Integer> getListAvailableSize(String id) {
        return productSizeRepository.findAllSizeOfProduct(id);
    }

    @Override
    public void createSizeCount(CreateSizeCountRequest createSizeCountRequest) {

        //Kiểm trả size
        boolean isValid = false;
        for (int size : Contant.SIZE_VN) {
            if (size == createSizeCountRequest.getSize()) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new BadRequestException("Size không hợp lệ");
        }

        //Kiểm trả sản phẩm có tồn tại
        Optional<Product> product = productRepository.findById(createSizeCountRequest.getProductId());
        if (product.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm trong hệ thống!");
        }

//        Optional<ProductSize> productSizeOld = productSizeRepository.getProductSizeBySize(createSizeCountRequest.getSize(),createSizeCountRequest.getProductId());

        ProductSize productSize = new ProductSize();
        productSize.setProductId(createSizeCountRequest.getProductId());
        productSize.setSize(createSizeCountRequest.getSize());
        productSize.setQuantity(createSizeCountRequest.getCount());

        productSizeRepository.save(productSize);
    }

    @Override
    public List<ProductSize> getListSizeOfProduct(String id) {
        return productSizeRepository.findByProductId(id);
    }

    @Override
    public List<ShortProductInfoDTO> getListProduct() {
        return productRepository.getListProduct();
    }

    @Override
    public List<ShortProductInfoDTO> getAvailableProducts() {
        return productRepository.getAvailableProducts();
    }

    @Override
    public boolean checkProductSizeAvailable(String id, int size) {
        ProductSize productSize = productSizeRepository.checkProductAndSizeAvailable(id, size);
        if (productSize != null) {
            return true;
        }
        return false;
    }

    @Override
    public List<ProductInfoDTO> checkPublicPromotion(List<ProductInfoDTO> products) {
        //Kiểm tra có khuyến mại
        Promotion promotion = promotionService.checkPublicPromotion();
        if (promotion != null) {
            //Tính giá sản phẩm khi có khuyến mại
            for (ProductInfoDTO product : products) {
                long discountValue = promotion.getMaximumDiscountValue();
                if (promotion.getDiscountType() == Contant.DISCOUNT_PERCENT) {
                    long tmp = product.getPrice() * promotion.getDiscountValue() / 100;
                    if (tmp < discountValue) {
                        discountValue = tmp;
                    }
                }
                long promotionPrice = product.getPrice() - discountValue;
                if (promotionPrice > 0) {
                    product.setPromotionPrice(promotionPrice);
                } else {
                    product.setPromotionPrice(0);
                }
            }
        }

        return products;
    }

    @Override
    public PageableDTO filterProduct(FilterProductRequest req) {

        PageUtil pageUtil = new PageUtil(Contant.LIMIT_PRODUCT_SHOP, req.getPage());

        //Lấy danh sách sản phẩm và tổng số sản phẩm
        int totalItems;
        List<ProductInfoDTO> products;

        if (req.getSizes().isEmpty()) {
            //Nếu không có size
            products = productRepository.searchProductAllSize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice(), Contant.LIMIT_PRODUCT_SHOP, pageUtil.calculateOffset());
            totalItems = productRepository.countProductAllSize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice());
        } else {
            //Nếu có size
            products = productRepository.searchProductBySize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice(), req.getSizes(), Contant.LIMIT_PRODUCT_SHOP, pageUtil.calculateOffset());
            totalItems = productRepository.countProductBySize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice(), req.getSizes());
        }

        //Tính tổng số trang
        int totalPages = pageUtil.calculateTotalPage(totalItems);

        return new PageableDTO(checkPublicPromotion(products), totalPages, req.getPage());

    }

    @Override
    public PageableDTO searchProductByKeyword(String keyword, Integer page) {
        // Validate
        if (keyword == null) {
            keyword = "";
        }
        if (page == null) {
            page = 1;
        }

        PageUtil pageInfo = new PageUtil(Contant.LIMIT_PRODUCT_SEARCH, page);

        //Lấy danh sách sản phẩm theo key
        List<ProductInfoDTO> products = productRepository.searchProductByKeyword(keyword, Contant.LIMIT_PRODUCT_SEARCH, pageInfo.calculateOffset());

        //Lấy số sản phẩm theo key
        int totalItems = productRepository.countProductByKeyword(keyword);

        //Tính số trang
        int totalPages = pageInfo.calculateTotalPage(totalItems);

        return new PageableDTO(checkPublicPromotion(products), totalPages, page);
    }

    @Override
    public Promotion checkPromotion(String code) {
        return promotionRepository.checkPromotion(code);
    }

    @Override
    public long getCountProduct() {
        return productRepository.count();
    }

    @Override
    public void updatefeedBackImages(String id, UpdateFeedBackRequest req) {
        // Check product exist
        Optional<Product> rs = productRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }

        Product product = rs.get();
        product.setImageFeedBack(req.getFeedBackImages());
        try {
            productRepository.save(product);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi cập nhật hình ảnh on feet");
        }
    }

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }
}
