//package com.bn.ninjatrader.logical.expression.condition;
//
//import com.bn.ninjatrader.logical.expression.model.Data;
//import com.bn.ninjatrader.logical.expression.operation.Constant;
//import com.bn.ninjatrader.logical.expression.operation.Operation;
//import com.bn.ninjatrader.logical.expression.operation.Variable;
//import com.bn.ninjatrader.logical.expression.operator.InequalityOperator;
//import com.bn.ninjatrader.logical.expression.util.TestUtil;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.collect.Sets;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.Set;
//
//import static com.bn.ninjatrader.logical.expression.operator.InequalityOperator.*;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// * Created by Brad on 8/2/16.
// */
//public class BasicConditionTest {
//  private static final Logger LOG = LoggerFactory.getLogger(BasicConditionTest.class);
//
//  private final Data data = new MockData();
//  private final Variable var1 = Variable.of("DataType1");
//  private final Variable var2 = Variable.of("DataType2");
//
//  @Test
//  public void testEqualsCondition_shouldReturnTrueIfEqual() {
//    assertThat(isConditionMatch(0, EQ, -0)).isTrue();
//    assertThat(isConditionMatch(4.2345, EQ, 4.2345)).isTrue();
//    assertThat(isConditionMatch(4.2345, EQ, 4.23450)).isTrue();
//    assertThat(isConditionMatch(4.2345, EQ, 4.23456)).isFalse();
//  }
//
//  @Test
//  public void testGreaterThanCondition_shouldReturnTrueIfGreater() {
//    assertThat(isConditionMatch(4.2345, GT, 4.2344)).isTrue();
//    assertThat(isConditionMatch(4.2345, GT, 4.2345)).isFalse();
//    assertThat(isConditionMatch(4.2345, GT, 5.0)).isFalse();
//  }
//
//  @Test
//  public void testLessThanCondition_shouldReturnTrueIfLess() {
//    assertThat(isConditionMatch(4.2345, LT, 4.2346)).isTrue();
//    assertThat(isConditionMatch(-4.2345, LT, -4.2344)).isTrue();
//    assertThat(isConditionMatch(4.2345, LT, 4.2345)).isFalse();
//    assertThat(isConditionMatch(4.2345, LT, 3.0)).isFalse();
//  }
//
//  @Test
//  public void testGetVariablesOfConstantCondition_shouldReturnZeroVariables() {
//    Condition condition = new BasicCondition(Constant.of(3.0), GT, Constant.of(1.0));
//    assertThat(condition.getVariables()).hasSize(0);
//  }
//
//  @Test
//  public void testGetVariablesOfVariableCondition_shouldReturnAllVariables() {
//    Condition condition = new BasicCondition(var2, GT, var1);
//    assertThat(condition.getVariables()).containsOnly(var1, var2);
//  }
//
//  @Test
//  public void testEqualsWithSameObjects_shouldReturnEqual() {
//    assertThat(new BasicCondition(var2, GT, var1))
//        .isEqualTo(new BasicCondition(var2, GT, var1));
//    assertThat(new BasicCondition(var2, EQ, Constant.of(100)))
//        .isEqualTo(new BasicCondition(var2, EQ, Constant.of(100)));
//    assertThat(new BasicCondition(Constant.of(555), LT, Constant.of(100)))
//        .isEqualTo(new BasicCondition(Constant.of(555), LT, Constant.of(100)));
//  }
//
//  @Test
//  public void testEqualsWithDiffObjects_shouldReturnNotEqual() {
//    assertThat(new BasicCondition(var2, GT, var1))
//        .isNotEqualTo(new BasicCondition(var2, GT, var2));
//    assertThat(new BasicCondition(var2, EQ, Constant.of(100)))
//        .isNotEqualTo(new BasicCondition(var2, EQ, Constant.of(101)));
//    assertThat(new BasicCondition(Constant.of(555), LT, Constant.of(100)))
//        .isNotEqualTo(new BasicCondition(Constant.of(444), LT, Constant.of(100)));
//  }
//
//  @Test
//  public void testHashCode_shouldReturnDiffHashCodesForDiffObjects() {
//    final Set<BasicCondition> set = Sets.newHashSet();
//    set.add(new BasicCondition(var2, GT, var1));
//    set.add(new BasicCondition(var2, GT, var1)); // Add duplicate
//    set.add(new BasicCondition(var2, EQ, Constant.of(100)));
//    set.add(new BasicCondition(Constant.of(555), LT, Constant.of(100)));
//    set.add(new BasicCondition(Constant.of(555), LT, Constant.of(100))); // Add duplicate
//
//    assertThat(set).hasSize(3);
//  }
//
//  @Test
//  public void testSerializeDeserialize_shouldProduceEqualObject() throws IOException {
//    final ObjectMapper om = TestUtil.om();
//    final Condition condition = new BasicCondition(Constant.of(3.0), GT, Constant.of(1.0));
//    final String json = om.writeValueAsString(condition);
//    assertThat(om.readValue(json, Condition.class)).isEqualTo(condition);
//  }
//
//  @Test
//  public void testToString_shouldConvertVariablesToConstants() {
//    final Data data = mock(Data.class);
//    when(data.get(var1)).thenReturn(10d);
//    when(data.get(var2)).thenReturn(20d);
//    assertThat(BasicCondition.of(var1, LT, var2).toString(data)).isEqualTo("10.0 < 20.0");
//  }
//
//  /**
//   * Creates a basic condition and checks if it matches.
//   * @param lhs Left-hand-side operation
//   * @param operator Inequality operator
//   * @param rhs Right-hand-side operation
//   * @return true if condition matches.
//   */
//  private boolean isConditionMatch(double lhs, InequalityOperator operator, double rhs) {
//    return isConditionMatch(Constant.of(lhs), operator, Constant.of(rhs));
//  }
//
//  /**
//   * Creates a basic condition and checks if it matches.
//   * @param lhs Left-hand-side operation
//   * @param operator Inequality operator
//   * @param rhs Right-hand-side operation
//   * @return true if condition matches.
//   */
//  private boolean isConditionMatch(Operation lhs, InequalityOperator operator, Operation rhs) {
//    return new BasicCondition(lhs, operator, rhs).isMatch(data);
//  }
//}
