package com.nhutkhuong1.application.service.impl;

import com.github.slugify.Slugify;
import com.nhutkhuong1.application.config.Contant;
import com.nhutkhuong1.application.entity.Post;
import com.nhutkhuong1.application.entity.User;
import com.nhutkhuong1.application.model.dto.PageableDTO;
import com.nhutkhuong1.application.model.dto.PostDTO;
import com.nhutkhuong1.application.model.request.CreatePostRequest;
import com.nhutkhuong1.application.repository.PostRepository;
import com.nhutkhuong1.application.utils.PageUtil;
import com.nhutkhuong1.application.exception.BadRequestException;
import com.nhutkhuong1.application.exception.InternalServerException;
import com.nhutkhuong1.application.exception.NotFoundException;
import com.nhutkhuong1.application.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;

    @Override
    public PageableDTO adminGetListPost(String title, String status, int page) {
        PageUtil pageUtil = new PageUtil(Contant.LIMIT_POST, page);

        // Get list posts and totalItems
        List<PostDTO> posts = postRepository.adminGetListPost(title, status, Contant.LIMIT_POST, pageUtil.calculateOffset());
        int totalItems = postRepository.countPostFilter(title, status);

        int totalPages = pageUtil.calculateTotalPage(totalItems);

        return new PageableDTO(posts, totalPages, pageUtil.getPage());
    }

    @Override
    public Post createPost(CreatePostRequest createPostRequest, User user) {
        Post post = new Post();

        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        Slugify slg = new Slugify();
        post.setSlug(slg.slugify(createPostRequest.getTitle()));
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        post.setCreatedBy(user);

        if (createPostRequest.getStatus() == Contant.PUBLIC_POST) {
            // Public post
            if (createPostRequest.getDescription().isEmpty()) {
                throw new BadRequestException("Để công khai bài viết vui lòng nhập mô tả");
            }
            if (createPostRequest.getImage().isEmpty()) {
                throw new BadRequestException("Vui lòng chọn ảnh cho bài viết trước khi công khai");
            }
            post.setPublishedAt(new Timestamp(System.currentTimeMillis()));
        } else {
            if (createPostRequest.getStatus() != Contant.DRAFT_POST) {
                throw new BadRequestException("Trạng thái bài viết không hợp lệ");
            }
        }
        post.setDescription(createPostRequest.getDescription());
        post.setThumbnail(createPostRequest.getImage());
        post.setStatus(createPostRequest.getStatus());

        postRepository.save(post);

        return post;
    }

    @Override
    public void updatePost(CreatePostRequest createPostRequest, User user, Long id) {
        Optional<Post> rs = postRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Bài viết không tồn tại");
        }
        Post post = rs.get();
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        Slugify slug = new Slugify();
        post.setSlug(slug.slugify(createPostRequest.getTitle()));
        post.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        post.setModifiedBy(user);
        if (createPostRequest.getStatus() == Contant.PUBLIC_POST) {
            // Public post
            if (createPostRequest.getDescription().isEmpty()) {
                throw new BadRequestException("Để công khai bài viết vui lòng nhập mô tả");
            }
            if (createPostRequest.getImage().isEmpty()) {
                throw new BadRequestException("Vui lòng chọn ảnh cho bài viết trước khi công khai");
            }
            if (post.getPublishedAt() == null) {
                post.setPublishedAt(new Timestamp(System.currentTimeMillis()));
            }
        } else {
            if (createPostRequest.getStatus() != Contant.DRAFT_POST) {
                throw new BadRequestException("Trạng thái bài viết không hợp lệ");
            }
        }
        post.setDescription(createPostRequest.getDescription());
        post.setThumbnail(createPostRequest.getImage());
        post.setStatus(createPostRequest.getStatus());

        try {
            postRepository.save(post);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi cập nhật bài viết");
        }
    }

    @Override
    public void deletePost(long id) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            throw new NotFoundException("Bài viết không tồn tại");
        }
        try {
            postRepository.deleteById(id);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi xóa bài viết");
        }
    }

    @Override
    public Post getPostById(long id) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            throw new NotFoundException("Bài viết không tồn tại");
        }
        return post.get();
    }

    @Override
    public Page<Post> adminGetListPosts(String title, String status, Integer page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, Contant.LIMIT_POST, Sort.by("published_at").descending());
        return postRepository.adminGetListPosts(title, status, pageable);
    }

    @Override
    public List<Post> getLatesPost() {
        return postRepository.getLatesPosts(Contant.PUBLIC_POST, Contant.LIMIT_POST_NEW);
    }

    @Override
    public Page<Post> getListPost(int page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, Contant.LIMIT_POST_SHOP, Sort.by("publishedAt").descending());
        return postRepository.findAllByStatus(Contant.PUBLIC_POST, pageable);
    }

    @Override
    public List<Post> getLatestPostsNotId(long id) {
        return postRepository.getLatestPostsNotId(Contant.PUBLIC_POST,id, Contant.LIMIT_POST_RELATED);
    }

    @Override
    public long getCountPost() {
        return postRepository.count();
    }
}
