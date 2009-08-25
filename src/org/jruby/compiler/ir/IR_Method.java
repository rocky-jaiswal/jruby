package org.jruby.compiler.ir;

import java.util.List;
import java.util.ArrayList;
import org.jruby.compiler.ir.instructions.GET_CONST_Instr;
import org.jruby.compiler.ir.instructions.IR_Instr;
import org.jruby.compiler.ir.instructions.RECV_ARG_Instr;
import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.operands.MetaObject;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.opts.Optimization;

public class IR_Method extends IR_ScopeImpl
{
    public final String  _name;     // Ruby name 
    public final boolean _isInstanceMethod;

    public final Label _startLabel; // Label for the start of the method
    public final Label _endLabel;   // Label for the end of the method

    private boolean _optimizable;
    private List<Operand> _callArgs;

    public IR_Method(IR_Scope parent, IR_Scope lexicalParent, String name, String javaName, boolean isInstanceMethod)
    {
        this(parent, lexicalParent, name, isInstanceMethod);
        _callArgs = new ArrayList<Operand>();
    }

    public IR_Method(IR_Scope parent, IR_Scope lexicalParent, String name, boolean isInstanceMethod)
    {
        super(parent, lexicalParent);
        _name = name;
        _isInstanceMethod = isInstanceMethod;
        _startLabel = getNewLabel("_METH_START_");
        _endLabel   = getNewLabel("_METH_END_");
        _callArgs = new ArrayList<Operand>();
        _optimizable = true;
    }

    public void addInstr(IR_Instr i)
    {
        // Accumulate call arguments
        if (i instanceof RECV_ARG_Instr)
        _callArgs.add(i._result);

        super.addInstr(i);
    }

    public Operand[] getCallArgs() { return _callArgs.toArray(new Operand[_callArgs.size()]); }

    public Operand getConstantValue(String constRef)
    {
            // Constants are defined in classes & modules, not in methods!
            // So, this reference is actually defined in the containing class/module
        if (_parent instanceof MetaObject) {
            return ((MetaObject)_parent)._scope.getConstantValue(constRef);  
        }
        else {
            Variable cv = getNewVariable();
            addInstr(new GET_CONST_Instr(cv, _parent, constRef));
            return cv;
        }
    }

    public void setConstantValue(String constRef, Operand val) 
    {
        // SSS FIXME: Throw an exception here?
    }

    public void markUnoptimizable() { _optimizable = false; }

    public boolean isUnoptimizable() { return _optimizable; }

    public String toString() {
        return "Method: " +
                "\n  name: " + _name +
                super.toString();
    }

    public void optimize(Optimization opt)
    {
        super.optimize(opt);
        opt.run(this);
    }
}
