package com.lzhq.miaosha.service;

import com.lzhq.miaosha.dao.UserDao;
import com.lzhq.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    public int addUser(User user) {
        return userDao.insert(user);
    }
}
