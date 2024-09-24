package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    //创建符号表
    public LexicalAnalyzer(SymbolTable symbolTable)
    {
        this.symbolTable = symbolTable;
    }
    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
//    private  char[] buffer;
//    private int bufferSize = 1024;
//    private  int pos = 0;
//    private  int bytesRead = 0;
    public List<Token> tokens = new ArrayList<>();
    public String inputString = "";
    private FileInputStream fileInputStream;
    public void loadFile(String path) {
        // 词法分析前的缓冲区实现
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                inputString += line;
            }
            //System.out.println(sourceString);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    private enum STATES{
        IDLE,
        SKIP,
        FINAL,
        LETTER,LETTER_END,
        DIGIT,DIGIT_END,
        STAR,EXP,MULTI,
        COLON_START,ASSIGN,COLON,
        LESS,LE,NE,LT,
        EQ,
        GREATER,GE,GT,
        ADD,
        MINUS,
        RDIV,
        COMMA,
        SEMIC,
        ERROR
    }
    private STATES currentState;
    private StringBuilder currentIdentifier,currentNumber;
    public void run() {
        // TODO: 自动机实现的词法分析过程
        int i = 0;
        char ch;
        char[] word = inputString.toCharArray();
        currentIdentifier = new StringBuilder();
        currentNumber = new StringBuilder();
        currentState = STATES.IDLE;
        //main 中调用了loadfile
        List<Token> tokens = new ArrayList<>();
        while (i < inputString.length()) {
            ch = word[i];
            //现态到次态的转变
            switch (currentState) {
                case IDLE: {
                    if (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
                        //特殊字符
                        currentState = STATES.SKIP;
                    }
                    else if (Character.isLetter(ch)) {
                        //关键字和标识符
                        currentState = STATES.LETTER;
                    } else if (Character.isDigit(ch)) {
                        //数字
                        currentState = STATES.DIGIT;
                    } else {
                        switch (ch) {
                            case '*': {
                                currentState = STATES.STAR;
                                break;
                            }
                            case ':': {
                                currentState = STATES.COLON_START;
                                break;
                            }
                            case '<': {
                                currentState = STATES.LESS;
                                break;
                            }
                            case '>': {
                                currentState = STATES.GREATER;
                                break;
                            }
                        }
                    }
                    break;
                }
                case STAR: {
                    if (ch == '*') {
                        currentState = STATES.EXP;
                    } else {
                        currentState = STATES.MULTI;
                    }
                    break;
                }
                case COLON_START: {
                    if (ch == '=') {
                        currentState = STATES.ASSIGN;
                    } else {
                        currentState = STATES.COLON;
                    }
                    break;
                }
                case LESS: {
                    if (ch == '=') {
                        currentState = STATES.LE;
                    } else if (ch == '>') {
                        currentState = STATES.NE;
                    } else {
                        currentState = STATES.LT;
                    }
                    break;
                }
                case GREATER: {
                    if (ch == '=') {
                        currentState = STATES.GE;
                    } else {
                        currentState = STATES.GT;
                    }
                    break;
                }
                case LETTER: {
                    if (Character.isLetter(ch)) {
                        currentState = STATES.LETTER;
                    } else {
                        currentState = STATES.LETTER_END;
                    }
                    break;
                }
                case DIGIT: {
                    if (Character.isDigit(ch)){
                        currentState = STATES.DIGIT;
                    }else{
                        currentState = STATES.DIGIT_END;
                    }
                    break;
                }
                case LETTER_END: {
                    if (Character.isDigit(ch)){
                        currentState = STATES.DIGIT;
                    }else if (Character.isLetter(ch)){
                        currentState = STATES.LETTER;
                    }else if (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
                        currentState = STATES.SKIP;
                    }else{
                        currentState = STATES.IDLE;
                    }
                    break;
                }
                case DIGIT_END:{
                    if (Character.isDigit(ch)){
                        currentState = STATES.DIGIT;
                    }else if (Character.isLetter(ch)){
                        currentState = STATES.LETTER;
                    }else if (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
                        currentState = STATES.SKIP;
                    }else{
                        currentState = STATES.IDLE;
                    }
                    break;
                }
                case SKIP:{
                    if (Character.isDigit(ch)){
                        currentState = STATES.DIGIT;
                    }else if (Character.isLetter(ch)){
                        currentState = STATES.LETTER;
                    }else if (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
                        currentState = STATES.SKIP;
                    }else{
                        currentState = STATES.IDLE;
                    }
                    break;
                }
                default:{
                    currentState = STATES.ERROR;
                }
            }

            //状态机的输出
            switch (currentState) {
                case IDLE:{
                    break;
                }
                case SEMIC:{
                    tokens.add(Token.simple("Semicolon"));
                    break;
                }
                case EXP:{
                    tokens.add(Token.simple("EXP"));
                    break;
                }
                case MULTI:{
                    tokens.add(Token.simple("*"));
                    break;
                }
                case ASSIGN:{
                    tokens.add(Token.simple("ASSIGN"));
                    break;
                }
                case COLON:{
                    tokens.add(Token.simple(":"));
                    break;
                }
                case LE:{
                    tokens.add(Token.simple("LE"));
                    break;
                }
                case NE:{
                    tokens.add(Token.simple("NE"));
                    break;
                }
                case LT:{
                    tokens.add(Token.simple("LT"));
                    break;
                }
                case EQ:{
                    tokens.add(Token.simple("="));
                    break;
                }
                case GE:{
                    tokens.add(Token.simple("GE"));
                    break;
                }
                case GT:{
                    tokens.add(Token.simple("GT"));
                    break;
                }
                case ADD:{
                    tokens.add(Token.simple("+"));
                    break;
                }
                case MINUS:{
                    tokens.add(Token.simple("-"));
                    break;
                }
                case RDIV:{
                    tokens.add(Token.simple("/"));
                    break;
                }
                case COMMA:{
                    tokens.add(Token.simple(","));
                    break;
                }
                case LETTER:{
                    currentIdentifier.append(ch);
                    break;
                }
                case DIGIT:{
                    currentNumber.append(ch);
                    break;
                }
                case DIGIT_END: {
                    tokens.add(Token.normal("IntConst", currentNumber.toString()));
                    currentNumber.setLength(0);
                    break;
                }
                case LETTER_END: {
                    String key = currentIdentifier.toString();
                    if (TokenKind.isAllowed(key)) {
                        tokens.add(Token.simple(key));
                    } else {
                        //当前读入字符串为关键字
                        tokens.add(Token.normal("id",key));
                        if (!symbolTable.has(key)) {
                            symbolTable.add(key);
                        }
                    }
                    System.out.printf("%s\n",key);
                    currentIdentifier.setLength(0);
                    break;
                }
                case SKIP:{
                    ++i;
                    break;
                }
                case ERROR:{
                    System.out.printf("error char : %c \n",ch);
                    break;
                }
                default:{
                    System.out.printf("default branch \n");
                    break;
                }
            }
            ++i;
            System.out.printf("current states: %s\n", currentState);
            //throw new NotImplementedException();
        }//while
        //插入结束符
        tokens.add(Token.eof());
    }
    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return
     * 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        //throw new NotImplementedException();
        return tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
