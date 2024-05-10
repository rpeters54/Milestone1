package instructions;

public class DriverDeclarationInstruction implements Instruction {

    @Override
    public String toString() {
        return "declare i8* @malloc(i64)\n"
                +"declare void @free(i8*)\n"
                +"declare i64 @printf(i8*, ...)\n"
                +"declare i64 @scanf(i8*, ...)\n"
                +"@.println = private unnamed_addr constant [5 x i8] c\"%ld\\0A\\00\", align 1\n"
                +"@.print = private unnamed_addr constant [5 x i8] c\"%ld \\00\", align 1\n"
                +"@.read = private unnamed_addr constant [4 x i8] c\"%ld\\00\", align 1\n"
                +"@.read_scratch = common global i64 0, align 8\n";
    }


    @Override
    public void substituteSource(Source original, Source replacement) {
        //do nothing
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        //do nothing
    }
}
