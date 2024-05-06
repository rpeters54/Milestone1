package instructions;

import ast.types.FunctionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhiInstruction implements Instruction {
    private String boundName;
    private Register result;
    private List<Source> members;

    public PhiInstruction(String boundName, Register result, List<Source> members) {
        this.boundName = boundName;
        this.result = result;
        this.members = members;
    }

    public String getBoundName() {
        return boundName;
    }

    public Register getResult() {
        return result;
    }

    public void setBoundName(String name) {
        this.boundName = name;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    public void setMembers(List<Source> members) {
        this.members = members;
    }

    public void addMember(Source member) {
        members.add(member);
    }

    public boolean isRedundant() {
        members.removeIf(member -> member.equals(result));
        Set<Source> set = new HashSet<>(members);
        members = new ArrayList<>(set);
        return members.size() == 1;
    }

    public List<Source> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        String start = String.format("%s = phi %s ", result.getValue(), result.getTypeString());
        StringBuilder builder = new StringBuilder(start);
        for (Source member : members) {
            String memberString = String.format("[%s, %%%s], ", member.getValue(), member.getLabel().getValue());
            builder.append(memberString);
        }
        builder.delete(builder.length()-2, builder.length());
        return builder.toString();
    }

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(result)) {
            if (replacement instanceof Register) {
                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("PhiInstruction: Tried to replace necessary Register with Source");
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equals(item)) {
                replacement.setLabel(members.get(i).getLabel());
                members.set(i, replacement);
            }
        }
    }
}
