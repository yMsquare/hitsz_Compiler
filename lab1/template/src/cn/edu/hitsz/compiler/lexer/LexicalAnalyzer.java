package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Array;
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
    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    private  char[] buffer;
    private int bufferSize = 1024;
    private  int pos = 0;
    private  int bytesRead = 0;
    private FileInputStream fileInputStream;
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        try{
            fileInputStream = new FileInputStream(path);
            buffer = new char[bufferSize];
            fillBuffer();
        }catch(IOException e){
            e.printStackTrace();
        }
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        throw new NotImplementedException();
    }
    //填充缓冲区
    private void fillBuffer() throws IOException {
        byte[] bytesBuffer = new byte[bufferSize];
        bytesRead = fileInputStream.read(bytesBuffer);
        if(bytesRead != -1){
            for (int i = 0; i< bytesRead;i++){
                buffer[i] = (char)bytesBuffer[i];
            }
        }
    }
    //逐字符读取下一个
    private char nextChar() throws IOException {
        if(pos >= bytesRead){
            fillBuffer();
            if(bytesRead == -1){
                return '\0';
            }
            pos = 0;
        }
        return buffer[pos++];
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
        currentIdentifier = new StringBuilder();
        currentNumber = new StringBuilder();
        currentState = STATES.IDLE;
        //main 中调用了loadfile
        List<Token> tokens = new ArrayList<>();
        char ch;
        while (ch != '\0') {
            try {
                ch = nextChar();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(ch == '\0'){
                break;
            }
            //现态到次态的转变
            switch (currentState) {
                case IDLE: {
                    if (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
                        //特殊字符
                        currentState = STATES.SKIP;
                    }
                    //else if (ch == ',' || ch == ';' || ch == '=' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '(' || ch == ')') {
                        //仅判断一个字符便可以得出结果的情况
                      //  currentState = STATES.FINAL;
                   // }
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
                        currentIdentifier.append(ch);
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
                    currentState = STATES.IDLE;
                    break;
                }
                case DIGIT_END:{
                    currentState = STATES.IDLE;
                }
            }


            switch (currentState) {
                case SEMIC:{
                    tokens.add(Token.simple("SEMIC"));
                    break;
                }
                case EXP:{
                    tokens.add(Token.simple("EXP"));
                    break;
                }
                case MULTI:{
                    tokens.add(Token.simple("MULTI"));
                    break;
                }
                case ASSIGN:{
                    tokens.add(Token.simple("ASSIGN"));
                    break;
                }
                case COLON:{
                    tokens.add(Token.simple("COLON"));
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
                    tokens.add(Token.simple("EQ"));
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
                    tokens.add(Token.simple("ADD"));
                    break;
                }
                case MINUS:{
                    tokens.add(Token.simple("MINUS"));
                    break;
                }
                case RDIV:{
                    tokens.add(Token.simple("RDIV"));
                    break;
                }
                case COMMA:{
                    tokens.add(Token.simple("COMMA"));
                    break;
                }
                case DIGIT_END: {
                    tokens.add(Token.simple(currentNumber.toString()));  // 添加标识符 Token
                    break;
                    //return
                }
                case LETTER_END: {
                    tokens.add(Token.simple(currentIdentifier.toString()));  // 添加标识符 Token
                    break;
                }
                default:{
                    break;
                }
            }
            throw new NotImplementedException();
        }
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
        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
