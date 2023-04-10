import random
import numpy as np
import math
from enum import Enum
import simulation


class Behavior(Enum):
    SEEKING = 1
    TRACING = 2


class Cat:
    def __init__(self, flag, position, velocity, v_max):
        self._position = position
        self.flag = flag
        self._velocity = velocity
        self._v_max = v_max
        self._dimension_size = len(self._position)

    def evaluate(self, function):
        return function(self._position), self._position

    def move(self, function, best_pos):
        if self.flag == Behavior.SEEKING:
            candidate_moves = []

            for j in range(simulation.SMP):
                position_copy = self._position.copy()
                srd = int(np.random.uniform(0, self._dimension_size + 1, 1))
                cdc = int(self._dimension_size * simulation.CDC / 100)
                pos = (srd + cdc) % len(position_copy)

                if j > 0:
                    if srd < pos:
                        position_reverse = position_copy[srd:pos]
                    else:
                        position_reverse = position_copy[pos:srd]
                    position_reverse.reverse()
                    if srd < pos:
                        position_copy[srd:pos] = position_reverse
                    else:
                        position_copy[pos:srd] = position_reverse
                candidate_moves.append(list(position_copy))

            fitness_values = [function(candidate) for candidate in candidate_moves]

            fit_min = min(fitness_values)
            fit_max = max(fitness_values)

            if fit_max == fit_min:
                probabilities = [1 for value in fitness_values]
            else:
                probabilities = [abs(value - fit_min) / (fit_max - fit_min) for value in fitness_values]
            prob_sum = sum(probabilities)
            probabilities = list(map(lambda prob: float(prob / prob_sum), probabilities))

            next_position_idx = np.random.choice(simulation.SMP, 1, p=probabilities)[0]
            self._position = candidate_moves[next_position_idx]

        elif self.flag == Behavior.TRACING:
            r1 = random.random()
            self._velocity = Cat.add_vel(Cat.multiply(simulation.w1, self._velocity),
                                         Cat.multiply(r1 * simulation.c1, Cat.subtract(best_pos, self._position)))
            Cat.add_pos(self._position, self._velocity)

        else:
            raise Exception("Unreachable")

    @staticmethod
    def add_pos(pos, vel):
        for v in vel:
            idx_1 = pos.index(v[0])
            idx_2 = pos.index(v[1])
            pos[idx_1], pos[idx_2] = pos[idx_2], pos[idx_1]

    @staticmethod
    def add_vel(vel1: list, vel2: list):
        if vel1 == [] and vel2 == []:
            return []
        new_vel = vel1.copy() + vel2.copy()
        # print("length of combined velocities", len(new_vel))
        flag = False
        for idx1, value1 in enumerate(new_vel):
            for idx2, value2 in enumerate(new_vel):
                if idx1 == idx2:
                    continue
                elif (value1[0] in value2 or value1[1] in value2) and value1 != value2:
                    flag = True
                elif value1 == value2:
                    if flag:
                        flag = False
                    else:
                        new_vel.pop(idx1)
                        new_vel.pop(idx2 - 1)
                        break
        return new_vel

    @staticmethod
    def subtract(pos1, pos2):
        vel = []
        pos1_copy = pos1.copy()
        pos2_copy = pos2.copy()
        if len(pos2_copy) < len(pos1_copy):
            pos1_copy, pos2_copy = pos2_copy, pos1_copy
        while pos1_copy[:len(pos1_copy)] != pos2_copy[:len(pos1_copy)]:
            for i in range(len(pos1_copy)):
                if pos1_copy[i] != pos2_copy[i]:
                    j = pos2_copy.index(pos1_copy[i])
                    pos1_copy[i], pos1_copy[j] = pos1_copy[j], pos1_copy[i]
                    vel.append((pos1_copy[j], pos1_copy[i]))
        return vel

    @staticmethod
    def multiply(num, vel):
        if num == 0:
            return []
        int_part, dec_part = divmod(num, 1)
        new_len = int(dec_part * len(vel))
        # new_vel = vel.copy()
        # for i in range(int(int_part)):
            # new_vel = Cat.add_vel(new_vel, vel)
        return vel[:new_len]
