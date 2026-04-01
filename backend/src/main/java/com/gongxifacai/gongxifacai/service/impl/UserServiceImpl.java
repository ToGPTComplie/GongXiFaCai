package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import com.gongxifacai.gongxifacai.service.UserService;

import com.gongxifacai.gongxifacai.dto.UserInfo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.gongxifacai.gongxifacai.common.CommonErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

    @Override
    public UserInfo getUserInfo(Long userId) {

        return UserInfo.fromEntity(getUser(userId));
    }

    @Override
    public List<Holding> getUserHoldings(Long userId) {
        getUser(userId);
        return holdingRepository.findByUser_Id(userId);
    }

    @Override
    public User getUser(Long userId) {

        if (userId == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        return userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(USER_NOT_FOUND)
        );
    }

    @Override
    public User createUser(String name, BigDecimal initialCash) {

        User user = new User();
        if (name == null) {
            name = "";
        }
        user.setName(name);
        if (initialCash == null || initialCash.signum() <= 0) {
            initialCash = BigDecimal.ZERO;
        }
        user.setAvailableCash(initialCash);
        return userRepository.save(user);
    }

    @Override
    public Boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public User getReferenceById(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    public BigDecimal getAvailableCash(Long userId) {
        return getUser(userId).getAvailableCash();
    }
}
