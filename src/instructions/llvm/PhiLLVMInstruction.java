package instructions.llvm;

import ast.PhiTuple;
import instructions.Label;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class PhiLLVMInstruction extends AbstractLLVMInstruction {
    private String boundName;           // name that the phi is associated with in its Block's environment
    private List<Label> memberLabels;

    public PhiLLVMInstruction(String boundName, Register result, List<PhiTuple> members) {
        super(result, null);
        this.memberLabels = null;
        this.boundName = boundName;
        setMembers(members);
    }

    public PhiLLVMInstruction(String boundName, Register result) {
        super(result, null);
        this.memberLabels = null;
        this.boundName = boundName;
    }

    public String getBoundName() {
        return boundName;
    }

    public List<Label> getMemberLabels() {
        return memberLabels;
    }

    public Label getMemberLabel(int i) {
        return memberLabels.get(i);
    }

    public void setMemberLabels(List<Label> memberLabels) {
        this.memberLabels = memberLabels;
    }

    public void setBoundName(String name) {
        this.boundName = name;
    }

    public void setMembers(List<PhiTuple> members) {
        List<Source> sources = new ArrayList<>();
        List<Label> labels = new ArrayList<>();
        for (PhiTuple member : members) {
            sources.add(member.getSource());
            labels.add(member.getOrigin());
        }
        setSources(sources);
        this.memberLabels = labels;
    }

    public void addMember(PhiTuple member) {
        addSource(member.getSource());
        this.memberLabels.add(member.getOrigin());
    }

    public boolean isRedundant() {
        List<Source> currentSources = getSources();
        List<Source> sources = new ArrayList<>();
        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < currentSources.size(); i++) {
            if (currentSources.get(i).equals(getResult()))
                continue;
            boolean check = true;
            for (int j = 0; j < sources.size(); j++) {
                if (currentSources.get(i).equals(sources.get(j))
                && memberLabels.get(i).equals(labels.get(j))) {
                    check = false;
                    break;
                }
            }
            if (check) {
                sources.add(currentSources.get(i));
                labels.add(memberLabels.get(i));
            }
        }
        setSources(sources);
        memberLabels = labels;
        return memberLabels.size() == 1;
    }

    @Override
    public String toString() {
        String start = String.format("%s = phi %s ", getResult(), getResult().getTypeString());
        StringBuilder builder = new StringBuilder(start);
        for (int i = 0; i < memberLabels.size(); i++) {
            String memberString = String.format("[%s, %%%s], ",
                    getSource(i),
                    memberLabels.get(i).getName());
            builder.append(memberString);
        }
        builder.delete(builder.length()-2, builder.length());
        return builder.toString();
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        super.substituteLabel(original, replacement);
        for (int i = 0; i < memberLabels.size(); i++) {
            if (memberLabels.get(i).equals(original))
                memberLabels.set(i, replacement);
        }
    }

    @Override
    public List<ArmInstruction> toArm() {
        // should not be handled here
        return null;
    }
}
