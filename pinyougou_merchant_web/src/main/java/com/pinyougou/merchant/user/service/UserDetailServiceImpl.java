package com.pinyougou.merchant.user.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;


public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.根据商家用户id(username)查出商家信息
        TbSeller tbSeller = sellerService.findOne(username);
        if (tbSeller != null) {
            //2.获取商家的密码
            String password = tbSeller.getPassword();
            //3.获取商家的状态status,判断是否为1
            int status = Integer.parseInt(tbSeller.getStatus());
            //4.获取角色权限的集合
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
            return new User(username, password, status == 1 ? true : false, true, true, true, authorities);
        }
        return null;
    }
}
