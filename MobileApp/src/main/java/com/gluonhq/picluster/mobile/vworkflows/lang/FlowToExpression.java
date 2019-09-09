package com.gluonhq.picluster.mobile.vworkflows.lang;

import com.gluonhq.picluster.mobile.vworkflows.DoubleValue;
import com.gluonhq.picluster.mobile.vworkflows.ExecValue;
import com.gluonhq.picluster.mobile.vworkflows.FunctionValue;
import com.gluonhq.picluster.mobile.vworkflows.OperatorValue;
import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p>This class gets a specified flow and produces a mathematical expression that can be
 * easily evaluated.
 * </p>
 * <p>
 * <b>NOTE:</b>The flow has to use "data" flows exclusively. Currently, only the value objects in the
 * {@link com.gluonhq.picluster.mobile.vworkflows} package are supported.
 * </p>
 * 
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public final class FlowToExpression {

    public static class Result {

        private final String expression;
        private final boolean valid;

        public Result(String expression, boolean valid) {
            this.expression = expression;
            this.valid = valid;
        }
        
        public String getExpression() {
            return expression;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public String toString() {
            return "[expr: " + getExpression() + ", valid: "+isValid()+"]";
        }

    }

    /**
     * Predicate that determines whether the specified connector is not connected.
     */
    private static Predicate<Connector> notConnected = c ->
            c.getNode().getFlow().getConnections(c.getType()).getAllWith(c).isEmpty();

    /**
     * Predicate that determines whether the specified connector is connected.
     */
    private static Predicate<Connector> connected = c ->
            !c.getNode().getFlow().getConnections(c.getType()).getAllWith(c).isEmpty();

    /**
     * Predicate that determines whether the specified node is a terminal symbol.
     */
    private static Predicate<VNode> isTerminal = n -> n.getInputs().isEmpty();

    /**
     * Predicate that determines whether the specified node is a return value.
     */
    private static Predicate<VNode> isReturnValue = n -> {
        Predicate<Connector> predicate = connected;
        for (Connector connector : n.getOutputs()) {
            if (predicate.test(connector)) {
                return false;
            }
        }
        return true;
    };

    /**
     * Converts the specified flow to an expression string, e.g., {@code (2+3)*(3-2)}.
     * @param f flow
     * @return a math expression as String
     */
    public Result parseFlow(VFlow f) {

        List<VNode> retVal = new FilteredList<>(f.getNodes(), isReturnValue);

        if (retVal.size() != 1) {
            // TODO do we prefer exceptions?
            return new Result("ERROR: wrong number of return values. Got '" + retVal.size() + "', expected 1.",false);
        }

        List<VNode> terminals = new FilteredList<>(f.getNodes(), isTerminal);

        VNode root = retVal.get(0);

        return nodeToString(root);
        
    }

    private Result nodeToString(VNode n) {
        if (n.getValueObject().getValue() instanceof ExecValue) {

            List<VNode> inputs = getInputs(n, "data");

            if (inputs.size() < 1) {
                return new Result("ERROR: wrong number of connections. Got " + inputs.size() + ", expected 1.", false);
            }

            Result resL = nodeToString(inputs.get(0));
            return resL;
        } else if (n.getValueObject().getValue() == null) {
            return new Result("/*null*/", false);
        } else if (n.getValueObject().getValue() instanceof String) {

            List<VNode> inputs = getInputs(n, "data");

            if (inputs.isEmpty()) { // number or existing variable
                String v = (String) n.getValueObject().getValue();
                return new Result("" + v, true);
            }
            // variable
            String v = (String) n.getValueObject().getValue();
            Result resL = nodeToString(inputs.get(0));

            return new Result("" + v, true);
        } else if (n.getValueObject().getValue() instanceof DoubleValue) {
            DoubleValue v = (DoubleValue) n.getValueObject().getValue();
            return new Result(""+v.getValue(), true);
        } else if (n.getValueObject().getValue() instanceof FunctionValue) {
            FunctionValue v = (FunctionValue) n.getValueObject().getValue();

            // TODO ensure exactly one connected object

            Result res = nodeToString(getInputs(n, "data").get(0));

            switch (v.getValue()) {
                case SIN: return new Result("Math.sin(" + res.getExpression()+ ")", res.isValid());
                case COS: return new Result("Math.cos(" + res.getExpression()+ ")", res.isValid());
                case TAN: return new Result("Math.tan(" + res.getExpression()+ ")", res.isValid());
                case SQRT: return new Result("Math.sqrt(" + res.getExpression()+ ")", res.isValid());
                // TODO do we prefer exceptions?
                default:
                    return new Result("/*unsupported*/", false);
            }
        } else if (n.getValueObject().getValue() instanceof OperatorValue) {
            OperatorValue v = (OperatorValue) n.getValueObject().getValue();

            // TODO ensure exactly two connected object

            List<VNode> inputs = getInputs(n, "data");

            if (inputs.size() < 2) {
                return new Result("ERROR: wrong number of connections. Got " + inputs.size() + ", expected 2.", false);     
            }

            Result resL = nodeToString(inputs.get(0));
            Result resR = nodeToString(inputs.get(1));

            switch (v.getValue()) {
                case ADD:      return new Result("(" + resL.getExpression() + " + " + resR.getExpression()+")", resL.isValid() && resR.isValid());
                case SUBTRACT: return new Result("(" + resL.getExpression() + " - " + resR.getExpression()+")", resL.isValid() && resR.isValid());
                case MULTIPLY: return new Result("(" + resL.getExpression() + " * " + resR.getExpression()+")", resL.isValid() && resR.isValid());
                case DIVIDE:   return new Result("(" + resL.getExpression() + " / " + resR.getExpression()+")", resL.isValid() && resR.isValid());
                            
                default: 
                   // TODO do we prefer exceptions?
                    return new Result("/*unsupported*/", false);
            }
        }
        
        // TODO do we prefer exceptions?
        return new Result("/*unsupported*/", false);
    }

    /**
     * <p>
     * Returns all nodes that are connected to the inputs of the specified node.
     * </p>
     * <p>
     * <b>NOTE:</b> only the specified connection type is analyzed. Other connections
     * are ignored.
     * </p>
     * @param n node to analyze
     * @param type connection type, e.g., "data"
     * @return list of all nodes that are connected to the inputs of the specified node
     */
    private List<VNode> getInputs(VNode n, String type) {
        List<VNode> result = new ArrayList<>();
        for (Connector c : n.getInputs()) {
            List<VNode> inputs = new ArrayList<>();
            for (Connection conn : n.getFlow().getAllConnections().get(type).getAllWith(c)) {
                VNode node = conn.getSender().getNode();
                inputs.add(node);
            }
            result.addAll(inputs);  
        }

        return result;
    }
    
}
