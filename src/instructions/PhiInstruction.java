package instructions;

import ast.PhiTuple;
import ast.types.NullType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PhiInstruction implements Instruction {
    private String boundName;           // name that the phi is associated with in its Block's environment
    private Register result;            // the result register
    private List<PhiTuple> members;

    public PhiInstruction(String boundName, Register result, List<PhiTuple> members) {
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

    public void setMembers(List<PhiTuple> members) {
        this.members = members;
    }

    public void addMember(PhiTuple member) {
        members.add(member);
    }

    public boolean isRedundant() {
        List<PhiTuple> sources = new ArrayList<>();
        for (PhiTuple member : members) {
            if (member.getSource().equals(result))
                continue;
            boolean check = true;
            for (PhiTuple selected : sources) {
                if (selected.getSource().equals(member.getSource())) {
                    check = false;
                    break;
                }
            }
            if (check)
                sources.add(member);
        }
        members = sources;
        return members.size() == 1;
    }

    public List<PhiTuple> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        String start = String.format("%s = phi %s ", result.getValue(), result.getTypeString());
        StringBuilder builder = new StringBuilder(start);
        for (PhiTuple member : members) {
            String memberString = String.format("[%s, %%%s], ",
                    member.getSource().getValue(),
                    member.getOrigin().getValue());
            builder.append(memberString);
        }
        builder.delete(builder.length()-2, builder.length());
        return builder.toString();
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("PhiInstruction: Tried to replace necessary Register with Source");
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getSource().equals(original)) {
//                replacement.setLabel(members.get(i).getOrigin());
                members.set(i, new PhiTuple(replacement, members.get(i).getOrigin()));
            }
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (result.getLabel().equals(original))
            result.setLabel(replacement);
        for (PhiTuple member : members) {
            Source source = member.getSource();
            if (source.getLabel().equals(original))
                source.setLabel(replacement);
            if (member.getOrigin().equals(original))
                member.setOrigin(replacement);
        }
    }
}
