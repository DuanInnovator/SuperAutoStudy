package com.tihai.common;

/**
 * @Copyright : DuanInnovator
 * @Description :
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/27
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class Pair<F, S> {

    private final F first;
    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}


