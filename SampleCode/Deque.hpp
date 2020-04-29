/*
 * Implement your functionality here without changing test.cpp
 */

#ifndef Deque_DEFINE

#define Deque_DEFINE(typeParam)\
\
\
\
/*-------------------------------Iterator-----------------------------*/\
struct Deque_##typeParam##_Iterator\
{\
    /*-------------------------Fields----------------------*/\
    typeParam* address;\
    typeParam* minAddress;\
    typeParam* maxAddress;\
    /*-----------------------End Fields--------------------*/\
    \
    /*-------------------------Methods---------------------*/\
    void inc(Deque_##typeParam##_Iterator* it)\
    {\
        it->address = (it->address + 1);\
        \
        if(it->address > it->maxAddress)\
        {\
            it->address = it->minAddress;\
        }\
    };\
    \
    void dec(Deque_##typeParam##_Iterator* it)\
    {\
        it->address = (it->address - 1);\
        \
        if(it->address < it->minAddress)\
        {\
            it->address = it->maxAddress;\
        }\
    };\
    \
    typeParam& deref(Deque_##typeParam##_Iterator* it)\
    {\
        return *(it->address);\
    };\
    /*------------------------End Methods------------------*/\
};\
/*--------------------------End Iterator------------------------------*/\
\
\
\
\
\
/*-------------------------------Deque--------------------------------*/\
struct Deque_##typeParam\
{\
    /*-------------------------Fields----------------------*/\
    typeParam* mem;\
    \
    typeParam* frontMem;\
    \
    typeParam* backMem;\
    \
    size_t deqSize;\
    \
    char type_name[sizeof(("Deque_" #typeParam))] = "Deque_" #typeParam;\
    \
    bool (*f)(const typeParam &o1, const typeParam &o2);\
    \
    /*-----------------------End Fields--------------------*/\
    \
    /*-------------------------Methods---------------------*/\
    typeParam& front(Deque_##typeParam *deq)\
    {\
        if(deq->frontMem == (deq->mem + deq->deqSize - 1))\
        {\
            return *(deq->mem);\
        }\
        \
        else\
        {\
            return *(deq->frontMem + 1);\
        }\
    };\
    \
    typeParam& back(Deque_##typeParam *deq)\
    {\
        if(deq->backMem == deq->mem)\
        {\
            return *(deq->mem + deq->deqSize - 1);\
        }\
        \
        else\
        {\
            return *(deq->backMem - 1);\
        }\
    };\
    \
    size_t size(Deque_##typeParam *deq)\
    {\
        size_t t;\
        \
        if(deq->backMem > deq->frontMem)\
        {\
            t = (size_t) (deq->backMem - deq->frontMem - 1);\
        }\
        \
        if(deq->backMem < deq->frontMem)\
        {\
            t = (size_t) ((deq->backMem - deq->mem) + (deq->mem + deq->deqSize - 1 - deq->frontMem));\
        }\
        \
        return t;\
    };\
    \
    bool empty(Deque_##typeParam *deq)\
    {\
        bool t = false;\
        \
        if((deq->backMem > deq->frontMem) && ((deq->frontMem + 1) == deq->backMem))\
        {\
            t = true;\
        }\
         \
        else if((deq->backMem < deq->frontMem) && ((deq->backMem + deq->deqSize - 1) == deq->frontMem))\
        {\
            t = true;\
        }\
        \
        return t;\
    };\
    \
    void push_back(Deque_##typeParam *deq, typeParam t)\
    {\
        *(deq->backMem) = t;\
        \
        deq->backMem = (deq->backMem + 1);\
        \
        if(deq->backMem == (deq->mem + deq->deqSize))\
        {\
            deq->backMem = deq->mem;\
        }\
        \
        if(deq->frontMem == deq->backMem)\
        {\
            deq->resize(deq);\
        }\
    };\
    \
    void push_front(Deque_##typeParam *deq, typeParam t)\
    {\
        *(deq->frontMem) = t;\
        \
        deq->frontMem = (deq->frontMem - 1);\
        \
        if(deq->frontMem < deq->mem)\
        {\
            deq->frontMem = (deq->frontMem + deq->deqSize);\
        }\
        \
        if(deq->frontMem == deq->backMem)\
        {\
            deq->resize(deq);\
        }\
    };\
    \
    void pop_back(Deque_##typeParam *deq)\
    {\
        deq->backMem = (deq->backMem - 1);\
        \
        if(deq->backMem < deq->mem)\
        {\
            deq->backMem = (deq->backMem + deq->deqSize);\
        }\
    };\
    \
    void pop_front(Deque_##typeParam *deq)\
    {\
        deq->frontMem = (deq->frontMem + 1);\
        \
        if(deq->frontMem == (deq->mem + deq->deqSize))\
        {\
            deq->frontMem = deq->mem;\
        }\
    };\
    \
    typeParam& at(Deque_##typeParam *deq, int i)\
    {\
        typeParam* t = (deq->frontMem + 1 + i);\
        \
        if(t >= (deq->mem + deq->deqSize))\
        {\
            t = t - deq->deqSize;\
        }\
        \
        return *(t);\
    };\
    \
    void clear(Deque_##typeParam *deq)\
    {\
        deq->frontMem = deq->mem;\
        \
        deq->backMem = (deq->mem + 1);\
        \
    };\
    \
    void dtor(Deque_##typeParam *deq)\
    {\
        free(deq->mem);\
    };\
    \
    Deque_##typeParam##_Iterator begin(Deque_##typeParam *deq)\
    {\
        Deque_##typeParam##_Iterator t;\
        \
        t.address = (deq->frontMem + 1);\
        \
        if(t.address == (deq->mem + deq->deqSize))\
        {\
            t.address = deq->mem;\
        }\
        \
        t.minAddress = deq->mem;\
        \
        t.maxAddress = (deq->mem + deq->deqSize - 1);\
        \
        return t;\
    };\
    \
    Deque_##typeParam##_Iterator end(Deque_##typeParam *deq)\
    {\
        Deque_##typeParam##_Iterator t;\
        \
        t.address = deq->backMem ;\
        \
        if(t.address < deq->mem)\
        {\
            t.address = (t.address + deq->deqSize);\
        }\
        \
        t.minAddress = deq->mem;\
        \
        t.maxAddress = (deq->mem + deq->deqSize - 1);\
        \
        return t;\
    };\
    \
    void sort(Deque_##typeParam *deq, Deque_##typeParam##_Iterator a, Deque_##typeParam##_Iterator b)\
    {\
        if(a.address == b.address){/*Stop warnings, un-needed parameters...*/;}\
        \
        typeParam* array = (typeParam*) malloc(sizeof(typeParam) * deq->size(deq));\
        /*\
        int x = 0;\
        \
        for(size_t i = 0; i < deq->size(deq); i++)\
        {\
            array[x] = deq->at(deq, x);\
            \
            x++;\
        }*/\
        \
        if(deq->frontMem < deq->backMem)\
        {\
            memcpy(array, (deq->frontMem + 1), (sizeof(typeParam) * deq->size(deq)));\
        }\
        \
        else\
        {\
            memcpy(array, (deq->frontMem + 1), \
            (sizeof(typeParam) * ((deq->mem + deq->deqSize - 1) - deq->frontMem)));\
            \
            memcpy(array + ((deq->mem + deq->deqSize - 1) - deq->frontMem), \
            deq->mem, sizeof(typeParam) * (deq->size(deq) - ((deq->mem + deq->deqSize - 1) - deq->frontMem)));\
        }\
        \
        std::sort(array, array + deq->size(deq), deq->f);\
        \
        /*x = 0;\
        \
        for(size_t i = 0; i < deq->size(deq); i++)\
        {\
            deq->at(deq, x) = array[x];\
            \
            x++;\
        }*/\
        \
        if(deq->frontMem < deq->backMem)\
        {\
            memcpy((deq->frontMem + 1), array, (sizeof(typeParam) * deq->size(deq)));\
        }\
        \
        else\
        {\
            memcpy((deq->frontMem + 1), array,  \
            (sizeof(typeParam) * ((deq->mem + deq->deqSize - 1) - deq->frontMem)));\
            \
            memcpy(deq->mem, array + ((deq->mem + deq->deqSize - 1) - deq->frontMem), \
            sizeof(typeParam) * (deq->size(deq) - ((deq->mem + deq->deqSize - 1) - deq->frontMem)));\
        }\
        \
        free(array);\
    };\
    \
    void resize(Deque_##typeParam *deq)\
    {\
        deq->deqSize = deq->deqSize * 2;\
        \
        typeParam* temp = (typeParam*) malloc((sizeof(typeParam) * deq->deqSize));\
        \
        typeParam* newIndex = (temp + 1);\
        \
        typeParam* oldIndex = (deq->frontMem + 1);\
        \
        if(oldIndex == (deq->mem + (deq->deqSize / 2)))\
        {\
            oldIndex = deq->mem;\
        }\
        \
        for(size_t k = 0; k < ((deq->deqSize / 2) - 1); k++)\
        {\
            *newIndex = *oldIndex;\
            \
            oldIndex = oldIndex + 1;\
            \
            newIndex = newIndex + 1;\
            \
            if(oldIndex == (deq->mem + (deq->deqSize / 2)))\
            {\
                oldIndex = deq->mem;\
            }\
        }\
        \
        deq->backMem = temp + ((deq->deqSize / 2) - 1) + 1;\
        \
        deq->frontMem = temp;\
        \
        free(deq->mem);\
        \
        deq->mem = temp;\
        \
    };\
    /*------------------------End Methods------------------*/\
};\
/*-----------------------------End Deque------------------------------*/\
\
\
\
\
\
/*--------------------Additional Required Functions-------------------*/\
void Deque_##typeParam##_ctor(Deque_##typeParam *deq, bool (*f)(const typeParam &o1, const typeParam &o2))\
{\
    deq->deqSize = 2;\
    deq->mem = (typeParam*) malloc(sizeof(typeParam) * deq->deqSize);\
    deq->frontMem = deq->mem;\
    deq->backMem = (deq->mem + 1);\
    deq->f = f;\
}\
\
bool Deque_##typeParam##_equal(Deque_##typeParam deq1, Deque_##typeParam deq2)\
{\
    bool t = true;\
    \
    if(deq1.size(&deq1) == deq2.size(&deq2))\
    {\
        int index = 0;\
        \
        for(size_t i = 0; i < deq1.size(&deq1); i++)\
        {\
            if(deq1.f(deq1.at(&deq1, index), deq2.at(&deq2, index)) ||\
               deq1.f(deq2.at(&deq2, index), deq1.at(&deq1, index)))\
            {\
                t = false;\
                \
                i = deq1.size(&deq1);\
            }\
            \
            index++;\
        }\
    }\
    \
    else\
    {\
        t = false;\
    }\
    \
    return t;\
}\
\
bool Deque_##typeParam##_Iterator_equal(Deque_##typeParam##_Iterator t1, Deque_##typeParam##_Iterator t2)\
{\
    bool t = false;\
    \
    if(t1.address == t2.address)\
    {\
        t = true;\
    }\
    \
    return t;\
}\
\
/*------------------End Additional Required Functions-----------------*/\

#endif
