def eval(matrix):
    # structure of matrix:
    # matrix is a list of 4 lists:
    # 0. number (or name) of operation
    # 1. the job which the operation is a part of
    # 2. machine in which operation is processed
    # 3. processing time of operation
    fitness = 0
    solution = [20, 7, 5, 21, 25, 3, 2, 15, 11, 12, 17, 19, 18, 9, 16, 1, 10, 6, 23, 22, 4, 8, 14, 13, 24]
    for i in range(len(matrix)):
        if matrix[i] == solution[i]:
            fitness += 1
    return fitness
