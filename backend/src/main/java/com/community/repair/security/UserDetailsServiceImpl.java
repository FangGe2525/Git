package com.community.repair.security;

import com.community.repair.common.BusinessException;
import com.community.repair.common.ErrorCode;
import com.community.repair.entity.User;
import com.community.repair.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getRealName(), user.getRole());
    }

    public User findByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return user;
    }

    public LoginUser requireLoginUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getRealName(), user.getRole());
    }
}
