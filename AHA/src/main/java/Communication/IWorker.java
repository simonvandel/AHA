package Communication;

import java.util.Queue;

public interface IWorker<Input, Output> {
    void process(Input work);

    void registerOutputTo(Queue<Output> outputQueue);
}
