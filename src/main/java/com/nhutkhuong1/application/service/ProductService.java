package com.nhutkhuong1.application.service;

import com.nhutkhuong1.application.entity.Product;
import com.nhutkhuong1.application.entity.ProductSize;
import com.nhutkhuong1.application.entity.Promotion;
import com.nhutkhuong1.application.model.dto.DetailProductInfoDTO;
import com.nhutkhuong1.application.model.dto.PageableDTO;
import com.nhutkhuong1.application.model.dto.ProductInfoDTO;
import com.nhutkhuong1.application.model.dto.ShortProductInfoDTO;
import com.nhutkhuong1.application.model.request.CreateProductRequest;
import com.nhutkhuong1.application.model.request.CreateSizeCountRequest;
import com.nhutkhuong1.application.model.request.FilterProductRequest;
import com.nhutkhuong1.application.model.request.UpdateFeedBackRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {

    //Lấy sản phẩm
    Page<Product> adminGetListProduct(String id, String name, String category, String brand, Integer page);

    //Tạo sản phẩm
    Product createProduct(CreateProductRequest createProductRequest);

    //Sửa sản phẩm
    void updateProduct(CreateProductRequest createProductRequest, String id);

    //Lấy chi tiết sản phẩm
    Product getProductById(String id);

    //Xóa sản phẩm theo id
    void deleteProduct(String[] ids);

    //Xóa sản phẩm theo id
    void deleteProductById(String id);

    //Lấy sản phẩm bán nhiều nhất
    List<ProductInfoDTO> getListBestSellProducts();

    //Lấy sản phẩm mới nhất
    List<ProductInfoDTO> getListNewProducts();

    //Lấy sản phẩm xem nhiều
    List<ProductInfoDTO> getListViewProducts();

    //Lấy chi tiết sản phẩm theo id
    DetailProductInfoDTO getDetailProductById(String id);

    //Lấy sản phẩm liên quan
    List<ProductInfoDTO> getRelatedProducts(String id);

    //Lấy size có sẵn
    List<Integer> getListAvailableSize(String id);

    //Nhập số lượng theo size
    void createSizeCount(CreateSizeCountRequest createSizeCountRequest);

    //Lấy size của sản phẩm
    List<ProductSize> getListSizeOfProduct(String id);

    List<ShortProductInfoDTO> getListProduct();

    //Lấy sản phẩm có sẵn size
    List<ShortProductInfoDTO> getAvailableProducts();

    //Check size sản phẩm
    boolean checkProductSizeAvailable(String id, int size);

    //Kiểm tra sản phẩm có khuyến mại
    List<ProductInfoDTO> checkPublicPromotion(List<ProductInfoDTO> products);

    //Tìm kiếm sản phẩm theo danh mục, nhãn hiệu, giá
    PageableDTO filterProduct(FilterProductRequest req);

    //Tìm kiếm sản phẩm theo tên sản phẩm
    PageableDTO searchProductByKeyword(String keyword, Integer page);

    //Kiểm tra khuyến mại
    Promotion checkPromotion(String code);

    //Đếm số lượng sản phẩm
    long getCountProduct();

    //Thêm ảnh feedBack
    void updatefeedBackImages(String id, UpdateFeedBackRequest req);

    //Lấy tất cả sản phẩm
    List<Product> getAllProduct();

}
