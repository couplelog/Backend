package com.Lubee.Lubee.honey;

import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.user.domain.User;

import com.Lubee.Lubee.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.util.Date;

@Transactional  // rollback (DB 반영X)
@SpringBootTest
public class HoneyTest {

    Couple couple;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status;       // rollback (DB 반영X)

    @BeforeEach
    public void setUp() {
        Date date = new Date();
        couple = new Couple(null, null);
        coupleRepository.save(couple);

        User user1 = new User("username1", "password1", "mail1@gmail.com", date, Profile.c, "nick1");
        user1.setCouple(couple);
        User user2 = new User("username2", "password2", "mail2@gmail.com", date, Profile.d, "nick2");
        user2.setCouple(couple);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        couple.getUser().add(user1);
        couple.getUser().add(user2);

        coupleRepository.save(couple);

        status = transactionManager.getTransaction(new DefaultTransactionAttribute());
    }

    @AfterEach
    void tearDown() {
        transactionManager.rollback(status);    // transaction rollback
    }

    @Test
    @DisplayName("Total Honey 더하기 테스트")
    public void testAddTotalHoney() {

        couple.addTotalHoney();
        Assertions.assertEquals(1L, couple.getTotal_honey());
    }


    @Test
    @DisplayName("Total Honey 빼기 테스트")
    public void testSubtractTotalHoney() {

        couple.setTotal_honey(3L);
        couple.subtractTotalHoney();

        Assertions.assertEquals(2L, couple.getTotal_honey());
    }

}