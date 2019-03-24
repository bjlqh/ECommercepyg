package com.lqh.idworker;

import com.pinyougou.utils.IdWorker;
import org.junit.Test;

public class IdWorkerTest {
    @Test
    public void idWorkTest() {
        IdWorker idWorker = new IdWorker();
        String s = idWorker.nextId() + "";
        System.out.println(s+"的长度是"+s.length());
    }
}
