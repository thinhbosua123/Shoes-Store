package com.nhutkhuong1.application.controller.shop;

import com.nhutkhuong1.application.entity.Post;
import com.nhutkhuong1.application.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BlogController {

    @Autowired
    private PostService postService;

    @GetMapping("/tin-tuc")
    public String getPostPages(Model model,
                               @RequestParam(defaultValue = "1", required = false) Integer page) {

        Page<Post> posts = postService.getListPost(page);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("currentPage", posts.getPageable().getPageNumber() + 1);
        return "shop/post";
    }

    @GetMapping("/tin-tuc/{slug}/{id}")
    public String getPostDetail(Model model, @PathVariable long id){
        Post post = postService.getPostById(id);
        List<Post> postList = postService.getLatestPostsNotId(id);
        model.addAttribute("post",post);
        model.addAttribute("postList",postList);

        return "shop/post-detail";
    }

}
