import algorithm
import eval_function

NUM_RUNS = 1
NUM_CATS = 25
MR = 30     # percent
SMP = 5
c1 = 2.05
CDC = 80    # percent
v_max = 1
w1 = 0.7


if __name__ == '__main__':
    position = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25]
    cats = []
    for _ in range(NUM_RUNS):
        best, position, cats = algorithm.CSO.run(2000, getattr(eval_function, 'eval'), NUM_CATS, MR, v_max, position, [])
        print(best, position)
