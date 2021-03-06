package com.bn.ninjatrader.model.mongo.dao;

import com.bn.ninjatrader.common.model.User;
import com.bn.ninjatrader.model.mongo.guice.NtModelTestModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bn.ninjatrader.common.type.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Brad on 5/4/16.
 */
public class MongoUserDaoTest {
  private static final Logger LOG = LoggerFactory.getLogger(MongoUserDaoTest.class);

  private final User user = User.builder().userId("test").username("jd").firstname("John").lastname("Doe")
      .email("jh@email.com").mobile("911").addToWatchList("MEG").addRole(ADMIN).build();

  private static Injector injector;

  private MongoUserDao dao;

  @BeforeClass
  public static void setup() {
    injector = Guice.createInjector(new NtModelTestModule());
  }

  @Before
  public void before() {
    dao = injector.getInstance(MongoUserDao.class);
    dao.getMongoCollection().remove();
  }

  @Test
  public void testSaveAndFind_shouldReturnEqualObject() {
    dao.saveUser(user);

    assertThat(dao.findByUserId(user.getUserId())).hasValue(user);
  }

  @Test
  public void testUpdate_shouldUpdateUserDetails() {
    final User updated = User.builder().userId("test").firstname("Mary").lastname("Jane").build();

    // Save old user
    dao.saveUser(user);

    // Overwrite old with new
    dao.saveUser(updated);

    // Verify fields are updated
    final User updatedUser = dao.findByUserId("test").get();

    assertThat(updatedUser.getUsername()).isEqualTo("jd");
    assertThat(updatedUser.getFirstname()).isEqualTo("Mary");
    assertThat(updatedUser.getLastname()).isEqualTo("Jane");
    assertThat(updatedUser.getEmail()).isEqualTo("jh@email.com");
    assertThat(updatedUser.getWatchList()).containsExactly("MEG");
    assertThat(updatedUser.getRoles()).containsExactly(ADMIN);
  }

  @Test
  public void testSaveWithNoUserId_shouldGenerateUserIdAndSave() {
    final User user = User.builder().username("jd").firstname("John").lastname("Doe")
        .email("jh@email.com").mobile("911").addToWatchList("MEG").addRole(ADMIN).build();

    final User savedUser = dao.saveUser(user);

    assertThat(savedUser.getUserId()).isNotEmpty();
    assertThat(dao.findByUserId(savedUser.getUserId())).hasValue(savedUser);
  }
}
