#include <exception>
#include <stdarg.h>
#include <iostream>

namespace cs540
{
    class OutOfRange : public std::exception
    {
	    virtual const char * what() const throw()
        {
    	    return "Out Of Range";
        }
    };

    template <typename T, size_t size, size_t... Dims> 
    class Array
    {
        public: 

            static_assert((size > 0), "Array dimensions must be greater than 0...");    

            class FirstDimensionMajorIterator
            {
                public:

                    size_t getTotalSize(size_t s, ...)
                    {
                        size_t total = s;

                        va_list args;

                        va_start(args, s);
                    
                        for(size_t k = 0; k < sizeof...(Dims); k++) 
                        {
                            total *= va_arg(args, size_t);
                        }
                    
                        va_end(args);

                        return total;
                    };

                    FirstDimensionMajorIterator()
                    {
                        address = nullptr;

                        at = 0;

                        end = false;
                    };

                    FirstDimensionMajorIterator(const FirstDimensionMajorIterator &i) = default;

                    FirstDimensionMajorIterator &operator=(const FirstDimensionMajorIterator &i) = default;

                    friend bool operator==(const FirstDimensionMajorIterator &i1, const FirstDimensionMajorIterator &i2)
                    {
                        bool retVal = true;

                        if((i1.address != i2.address) || (i1.at != i2.at) || (i1.end != i2.end))
                        {
                            retVal = false;
                        }

                        return retVal;
                    };

                    friend bool operator!=(const FirstDimensionMajorIterator &i1, const FirstDimensionMajorIterator &i2)
                    {
                        return !(i1 == i2);
                    };

                    FirstDimensionMajorIterator &operator++()
                    {
                        at = at + 1;

                        if(at == getTotalSize(size, Dims...))
                        {
                            address = nullptr;

                            at = 0;

                            end = true;
                        }

                        return *this;
                    };

                    FirstDimensionMajorIterator operator++(int)
                    {
                        FirstDimensionMajorIterator it(*this);

                        at = at + 1;

                        if(at == getTotalSize(size, Dims...))
                        {
                            address = nullptr;

                            at = 0;

                            end = true;
                        }

                        return it;
                    };

                    T &operator*() const
                    {
                        return address[at];
                    };

                    size_t at;

                    T* address;

                    bool end;
            };

            class LastDimensionMajorIterator
            {
                public:

                    size_t getTotalSize(size_t s, ...)
                    {
                        size_t total = s;

                        va_list args;

                        va_start(args, s);
                    
                        for(size_t k = 0; k < sizeof...(Dims); k++) 
                        {
                            total *= va_arg(args, size_t);
                        }
                    
                        va_end(args);

                        return total;
                    };

                    void setSizes(size_t s, ...)
                    {
                        sizes[0] = s;

                        va_list args;

                        va_start(args, s);
                    
                        for(size_t k = 1; k < sizeof...(Dims) + 1; k++) 
                        {
                            sizes[k] = va_arg(args, size_t);
                        }
                    
                        va_end(args);
                    };

                    LastDimensionMajorIterator()
                    {
                        address = nullptr;

                        at = 0;

                        end = false;

                        for(size_t i = 0; i < sizeof...(Dims) + 1; i++)
                        {
                            current[i] = 0;
                        }

                        setSizes(size, Dims...);
                    };

                    LastDimensionMajorIterator(const LastDimensionMajorIterator &i)
                    {
                        address = i.address;

                        at = i.at;

                        end = i.end;

                        for(size_t x = 0; x < sizeof...(Dims) + 1; x++)
                        {
                            sizes[x] = i.sizes[x];
                            current[x] = i.current[x];
                        }
                    };

                    LastDimensionMajorIterator &operator=(const LastDimensionMajorIterator &i)
                    {
                        address = i.address;

                        at = i.at;

                        end = i.end;

                        for(size_t x = 0; x < sizeof...(Dims) + 1; x++)
                        {
                            sizes[x] = i.sizes[x];
                            current[x] = i.current[x];
                        }

                        return *this;
                    };

                    friend bool operator==(const LastDimensionMajorIterator &i1, const LastDimensionMajorIterator &i2)
                    {
                        bool retVal = true;

                        if((i1.address != i2.address) || (i1.at != i2.at) || (i1.end != i2.end))
                        {
                            retVal = false;
                        }

                        for(size_t i = 0; i < sizeof...(Dims) + 1; i++)
                        {
                            if((i1.sizes[i] != i2.sizes[i]) || (i1.current[i] != i2.current[i]))
                            {
                                retVal = false;
                            }
                        }

                        return retVal;
                    };

                    friend bool operator!=(const LastDimensionMajorIterator &i1, const LastDimensionMajorIterator &i2)
                    {
                        return !(i1 == i2);
                    };

                    LastDimensionMajorIterator &operator++()
                    {
                        at = at + 1;

                        if(at == getTotalSize(size, Dims...))
                        {
                            address = nullptr;

                            at = 0;

                            end = true;

                            for(size_t i = 0; i < sizeof...(Dims) + 1; i++)
                            {
                                current[i] = 0;
                            }
                        }

                        else
                        {
                            increment();
                        }

                        return *this;
                    };

                    LastDimensionMajorIterator operator++(int)
                    {
                        LastDimensionMajorIterator it(*this);

                        at = at + 1;

                        if(at == getTotalSize(size, Dims...))
                        {
                            address = nullptr;

                            at = 0;

                            end = true;

                            for(size_t i = 0; i < sizeof...(Dims) + 1; i++)
                            {
                                current[i] = 0;
                            }
                        }

                        else
                        {
                            increment();
                        }

                        return it;
                    };

                    void increment()
                    {
                        current[sizeof...(Dims)] = current[sizeof...(Dims)] + 1;

                        for(size_t x = 0; x < sizeof...(Dims) + 1; x++)
                        {
                            if(current[sizeof...(Dims) - x] == sizes[x])
                            {
                                current[sizeof...(Dims) - x] = 0;

                                if(x < sizeof...(Dims))
                                {
                                    current[sizeof...(Dims) - x - 1] = current[sizeof...(Dims) - x - 1] + 1; 
                                }
                            }
                        }
                    }

                    T &operator*() const
                    {
                        size_t total = 0;
                        size_t multiply = 1;

                        for(size_t x = 0; x < sizeof...(Dims) + 1; x++)
                        {
                            total += (current[x] * multiply);

                            multiply = multiply * sizes[ sizeof...(Dims) - x];
                        }

                        return *(address + total);
                    };

                    size_t sizes[sizeof...(Dims) + 1];

                    size_t current[sizeof...(Dims) + 1];

                    size_t at;

                    T* address;

                    bool end;
            };

            typedef T ValueType;

            Array() = default;

            Array(const Array<T, size, Dims...> &a)
            {
                for(size_t i = 0; i < size; i++)
                {
                    arr[i] = a.arr[i];
                }
            };

            template <typename U>
            Array(const Array<U, size, Dims...> &a)
            {
                for(size_t i = 0; i < size; i++)
                {
                    arr[i] = a.arr[i];
                }
            }

            Array<T, size, Dims...> &operator=(const Array<T, size, Dims...> &a)
            {
                if(this != &a)
                {
                    for(size_t i = 0; i < size; i++)
                    {
                        arr[i] = a.arr[i];
                    }
                }

                return *this;
            };

            template <typename U>
            Array<T, size, Dims...> &operator=(const Array<U, size, Dims...> &a)
            {
                if(this != (Array<T, size, Dims...>*)&a)
                {
                    for(size_t i = 0; i < size; i++)
                    {
                        arr[i] = a.arr[i];
                    }
                }

                return *this;
            }

            Array<T, Dims...> &operator[](size_t i)
            {
                if((i >= 0) && (i < size))
                {
                    return arr[i];
                }

                else
                {
                    OutOfRange err;

                    throw err;
                }
            };

            const Array<T, Dims...> &operator[](size_t i) const
            {
                if((i >= 0) && (i < size))
                {
                    return arr[i];
                }

                else
                {
                    OutOfRange err;

                    throw err;
                }
            };

            FirstDimensionMajorIterator fmbegin()
            {
                FirstDimensionMajorIterator it;

                it.address = (T*)arr;

                return it;
            };

            FirstDimensionMajorIterator fmend()
            {
                FirstDimensionMajorIterator it;

                it.end = true;

                return it;
            };

            LastDimensionMajorIterator lmbegin()
            {
                LastDimensionMajorIterator it;

                it.address = (T*)arr;

                return it;
            };

            LastDimensionMajorIterator lmend()
            {
                LastDimensionMajorIterator it;

                it.end = true;

                return it;
            };

            Array<T, Dims...> arr[size];
    };

    template <typename T, size_t size> 
    class Array<T, size>
    {
        public:

            static_assert((size > 0), "Array dimensions must be greater than 0..."); 

            class FirstDimensionMajorIterator
            {
                public:

                    FirstDimensionMajorIterator()
                    {
                        address = nullptr;

                        at = 0;

                        end = false;
                    };

                    FirstDimensionMajorIterator(const FirstDimensionMajorIterator &i) = default;

                    FirstDimensionMajorIterator &operator=(const FirstDimensionMajorIterator &i) = default;

                    friend bool operator==(const FirstDimensionMajorIterator &i1, const FirstDimensionMajorIterator &i2)
                    {
                        bool retVal = true;

                        if((i1.address != i2.address) || (i1.at != i2.at) || (i1.end != i2.end))
                        {
                            retVal = false;
                        }

                        return retVal;
                    };

                    friend bool operator!=(const FirstDimensionMajorIterator &i1, const FirstDimensionMajorIterator &i2)
                    {
                        return !(i1 == i2);
                    };

                    FirstDimensionMajorIterator &operator++()
                    {
                        at = at + 1;

                        if(at == size)
                        {
                            address = nullptr;

                            at = 0;

                            end = true;
                        }

                        return *this;
                    };

                    FirstDimensionMajorIterator operator++(int)
                    {
                        FirstDimensionMajorIterator it(*this);

                        at = at + 1;

                        if(at == size)
                        {
                            address = nullptr;

                            at = 0;

                            end = true;
                        }

                        return it;
                    };

                    T &operator*() const
                    {
                        return address[at];
                    };

                    size_t at;

                    T* address;

                    bool end;
            };

            class LastDimensionMajorIterator
            {
                public:

                    LastDimensionMajorIterator()
                    {
                        address = nullptr;

                        at = 0;

                        end = false;
                    };

                    LastDimensionMajorIterator(const LastDimensionMajorIterator &i) = default;

                    LastDimensionMajorIterator &operator=(const LastDimensionMajorIterator &i) = default;

                    friend bool operator==(const LastDimensionMajorIterator &i1, const LastDimensionMajorIterator &i2)
                    {
                        bool retVal = true;

                        if((i1.address != i2.address) || (i1.at != i2.at) || (i1.end != i2.end))
                        {
                            retVal = false;
                        }

                        return retVal;
                    };

                    friend bool operator!=(const LastDimensionMajorIterator &i1, const LastDimensionMajorIterator &i2)
                    {
                        return !(i1 == i2);
                    };

                    LastDimensionMajorIterator &operator++()
                    {
                        at = at + 1;

                        if(at == size)
                        {
                            address = nullptr;

                            at = 0;

                            end = true;
                        }

                        return *this;
                    };

                    LastDimensionMajorIterator operator++(int)
                    {
                        LastDimensionMajorIterator it(*this);

                        at = at + 1;

                        if(at == size)
                        {
                            address = nullptr;

                            at = 0;

                            end = true;
                        }

                        return it;
                    };

                    T &operator*() const
                    {
                        return address[at];
                    };

                    size_t at;

                    T* address;

                    bool end;
            };

            typedef T ValueType;

            Array<T, size>() = default;

            Array<T, size>(const Array<T, size> &a)
            {
                for(size_t i = 0; i < size; i++)
                {
                    arr[i] = a.arr[i];
                }
            };

            template <typename U>
            Array<T, size>(const Array<U, size> &a)
            {
                for(size_t i = 0; i < size; i++)
                {
                    arr[i] = a.arr[i];
                }
            }

            Array<T, size> &operator=(const Array<T, size> &a)
            {
                if(this != &a)
                {
                    for(size_t i = 0; i < size; i++)
                    {
                        arr[i] = a.arr[i];
                    }
                }

                return *this;
            };

            template <typename U>
            Array<T, size> &operator=(const Array<U, size> &a)
            {
                if(this != (Array<T, size>*)&a)
                {
                    for(size_t i = 0; i < size; i++)
                    {
                        arr[i] = a.arr[i];
                    }
                }

                return *this;
            }

            T &operator[](size_t i)
            {
                if((i >= 0) && (i < size))
                {
                    return arr[i];
                }

                else
                {
                    OutOfRange err;

                    throw err;
                }
            };

            const T &operator[](size_t i) const
            {
                if((i >= 0) && (i < size))
                {
                    return arr[i];
                }

                else
                {
                    OutOfRange err;

                    throw err;
                }
            };

            FirstDimensionMajorIterator fmbegin()
            {
                FirstDimensionMajorIterator it;

                it.address = arr;

                return it;
            };

            FirstDimensionMajorIterator fmend()
            {
                FirstDimensionMajorIterator it;

                it.end = true;

                return it;
            };

            LastDimensionMajorIterator lmbegin()
            {
                LastDimensionMajorIterator it;

                it.address = arr;

                return it;
            };

            LastDimensionMajorIterator lmend()
            {
                LastDimensionMajorIterator it;

                it.end = true;

                return it;
            };

            T arr[size];
    };
}

