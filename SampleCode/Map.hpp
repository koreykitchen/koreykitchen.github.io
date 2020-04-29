#include <iostream>

namespace cs540
{
    template <typename Key_T, typename Mapped_T>
    class Map
    {
        public:
            typedef std::pair<const Key_T, Mapped_T> ValueType;

            class Node
            {
                public:
                    Node()
                    {
                        next = nullptr;
                        prev = nullptr;
                        val = nullptr;
                    };

                    Node(ValueType v)
                    {
                        next = nullptr;
                        prev = nullptr;
                        val = new ValueType(v);
                    };

                    ~Node()
                    {
                        if(val != nullptr)
                        {
                            delete val;
                        }
                    };

                    ValueType* val;
                    Node* next;
                    Node* prev;

                private:
                    //Nothing yet...
            };

            class Iterator
            {
                public:
                    Iterator() = default;

                    Iterator(const Iterator &) = default;

                    ~Iterator() = default;

                    Iterator& operator=(const Iterator &) = default;

                    Iterator &operator++()
                    {
                        node = node->next;

                        return  *this;
                    };

                    Iterator operator++(int)
                    {
                        Iterator i(*this);

                        node = node->next;

                        return i;
                    };

                    Iterator &operator--()
                    {
                        node = node->prev;

                        return  *this;
                    };

                    Iterator operator--(int)
                    {
                        Iterator i(*this);

                        node = node->prev;

                        return i;
                    };

                    ValueType &operator*() const
                    {
                        return *(node->val);
                    };

                    ValueType *operator->() const
                    {
                        return &(*(node->val));
                    };

                    Node* getNode() const
                    {
                        return node;
                    }

                    void setNode(Node* n)
                    {
                        node = n;
                    }

                private:
                    Node* node;
            };

            class ConstIterator
            {
                public:
                    ConstIterator() = default;

                    ConstIterator(const ConstIterator &) = default;

                    ConstIterator(const Iterator &i)
                    {
                        node = i.getNode();
                    };

                    ~ConstIterator() = default;

                    ConstIterator& operator=(const ConstIterator &) = default;

                    ConstIterator &operator++()
                    {
                        node = node->next;

                        return *this;
                    };

                    ConstIterator operator++(int)
                    {
                        ConstIterator i(*this);

                        node = node->next;

                        return i;
                    };

                    ConstIterator &operator--()
                    {
                        node = node->prev;

                        return *this;
                    };

                    ConstIterator operator--(int)
                    {
                        ConstIterator i(*this);

                        node = node->prev;

                        return i;
                    };

                    const ValueType &operator*() const
                    {
                        return *(node->val);
                    };

                    const ValueType *operator->() const
                    {
                        const ValueType* v = &(*(node->val));

                        return v;
                    };

                    Node* getNode() const
                    {
                        return node;
                    }

                    void setNode(Node* n)
                    {
                        node = n;
                    }

                private:
                    Node* node;
            };

            class ReverseIterator
            {
                public:
                    ReverseIterator() = default;

                    ReverseIterator(const ReverseIterator &) = default;

                    ~ReverseIterator() = default;

                    ReverseIterator& operator=(const ReverseIterator &) = default;

                    ReverseIterator &operator++()
                    {
                        node = node->prev;

                        return *this;
                    };

                    ReverseIterator operator++(int)
                    {
                        ReverseIterator i(*this);

                        node = node->prev;

                        return i;
                    };

                    ReverseIterator &operator--()
                    {
                        node = node->next;

                        return *this;
                    };

                    ReverseIterator operator--(int)
                    {
                        ReverseIterator i(*this);

                        node = node->next;

                        return i;
                    };

                    ValueType &operator*() const
                    {
                        return *(node->val);
                    };

                    ValueType *operator->() const
                    {
                        return &(*(node->val));
                    };

                    Node* getNode() const
                    {
                        return node;
                    }

                    void setNode(Node* n)
                    {
                        node = n;
                    }

                private:
                    Node* node;
            };

            Map()
            {
                head = nullptr;
                tail = nullptr;
                endNode = new Node();
                baseNode = new Node();
                mapSize = 0;
            };

            Map(const Map &m)
            {
                head = nullptr;
                tail = nullptr;
                endNode = new Node();
                baseNode = new Node();
                mapSize = 0;

                insert<ConstIterator>(m.begin(), m.end());
            };

            Map &operator=(const Map &m)
            {
                if(*this != m)
                {
                    clear();
                    insert<ConstIterator>(m.begin(), m.end());
                }

                return *this;
            };

            Map(std::initializer_list<std::pair<const Key_T, Mapped_T>> l)
            {
                head = nullptr;
                tail = nullptr;
                endNode = new Node();
                baseNode = new Node();
                mapSize = 0;

                insert<typename std::initializer_list<std::pair<const Key_T, Mapped_T>>::const_iterator>(l.begin(), l.end());
            };

            ~Map()
            {
                 Node* temp;

                while(head != nullptr && head != endNode)
                {
                    temp = head;

                    head = head->next;

                    delete temp;
                }

                if(endNode != nullptr)
                {
                    delete endNode;
                }

                if(baseNode != nullptr)
                {
                    delete baseNode;
                }

                head = nullptr;
                tail = nullptr;
                endNode = nullptr;
                baseNode = nullptr;
                mapSize = 0;
            };

            size_t size() const
            {
                return mapSize;
            };

            bool empty() const
            {
                bool ret = false;

                if(mapSize == 0)
                {
                    ret = true;
                }

                return ret;
            };

            Iterator begin()
            {
                Iterator i;

                if(empty())
                {
                    i.setNode(endNode);
                }

                else
                {
                    i.setNode(head);
                }

                return i;
            };

            Iterator end()
            {
                Iterator i;

                i.setNode(endNode);

                return i;
            };

            ConstIterator begin() const
            {
                ConstIterator i;

                if(empty())
                {
                    i.setNode(endNode);
                }

                else
                {
                    i.setNode(head);
                }

                return i;
            };

            ConstIterator end() const
            {
                ConstIterator i;

                i.setNode(endNode);

                return i;
            };

            ReverseIterator rbegin()
            {
                ReverseIterator i;

                if(empty())
                {
                    i.setNode(baseNode);
                }

                else
                {
                    i.setNode(head);
                }

                return i;
            };

            ReverseIterator rend()
            {
                ReverseIterator i;

                i.setNode(baseNode);

                return i;
            };

            Iterator find(const Key_T &k)
            {
                Iterator i;
                Node* curr = head;

                while(curr != nullptr && curr != endNode && !(curr->val->first == k))
                {
                    curr = curr->next;
                }

                if(curr == nullptr || curr == endNode)
                {
                    i.setNode(endNode);
                }

                else 
                {
                    i.setNode(curr);
                }

                return i;
            };

            ConstIterator find(const Key_T &k) const
            {
                ConstIterator i;
                Node* curr = head;

                while(curr != nullptr && curr != endNode && !(curr->val->first == k))
                {
                    curr = curr->next;
                }

                if(curr == nullptr || curr == endNode)
                {
                    i.setNode(endNode);
                }

                else 
                {
                    i.setNode(curr);
                }

                return i;
            };

            Mapped_T &at(const Key_T &k)
            {
                Node* curr = head;

                while(curr != nullptr && curr != endNode && !(curr->val->first == k))
                {
                    curr = curr->next;
                }

                if(curr == nullptr || curr == endNode)
                {
                    throw std::out_of_range("Out Of Range...");
                }

                return curr->val->second;

            };

            const Mapped_T &at(const Key_T &k) const
            {
                Node* curr = head;

                while(curr != nullptr && curr != endNode && !(curr->val->first == k))
                {
                    curr = curr->next;
                }

                if(curr == nullptr || curr == endNode)
                {
                    throw std::out_of_range("Out Of Range...");
                }

                else 
                {
                    return curr->val->second;
                }
            };

            Mapped_T &operator[](const Key_T &k)
            {
                Node* curr = head;

                while(curr != nullptr && curr != endNode && !(curr->val->first == k))
                {
                    curr = curr->next;
                }

                if(curr == nullptr || curr == endNode)
                {
                    Mapped_T m;

                    insert(std::make_pair(k , m));

                    return at(k);
                }

                else 
                {
                    return curr->val->second;
                }
            };

            std::pair<Iterator, bool> insert(const ValueType &v)
            {
                Iterator i = find(v.first);

                if(i.getNode() == endNode)
                {
                    Node* n = new Node(v);

                    if(tail == nullptr)
                    {
                        n->prev = baseNode;
                        n->next = nullptr;
                        head = n;
                        tail = n;
                        tail->next = endNode;
                        endNode->prev = tail;
                    }

                    else
                    {
                        Node* curr = head;

                        while(curr != endNode && curr != nullptr && curr->val->first < v.first)
                        {
                            curr = curr->next;
                        }

                        if(curr == nullptr || curr == endNode)
                        {
                            n->prev = tail;
                            n->next = endNode;
                            endNode->prev = n;
                            tail->next = n;
                            tail = n;
                        }

                        else if(curr == head)
                        {
                            n->prev = baseNode;
                            n->next = head;
                            head->prev = n;
                            head = n;
                        }

                        else //curr is greater, insert before
                        {
                            //set prev
                            curr->prev->next = n;

                            //set new
                            n->prev = curr->prev;
                            n->next = curr;

                            //set next(curr)
                            curr->prev = n;
                        }
                    }

                    mapSize += 1;

                    Iterator j;

                    j.setNode(n);

                    return (std::make_pair(j, true));
                }

                else
                {
                    return (std::make_pair(i, false));
                }
            };

            template <typename IT_T> 
            void insert(IT_T range_beg, IT_T range_end)
            {
                for(auto i = range_beg; i != range_end; i++)
                {
                    insert(*i);
                }
            }

            void erase(Iterator pos)
            {
                erase(pos.getNode()->val->first);
            };

            void erase(const Key_T &k) throw()
            {
                Iterator i = find(k);
                Node* n = i.getNode();

                if(n != endNode)
                {
                    //Found at head
                    if(n == head)
                    {
                        head = n->next;
                        head->prev = baseNode;
                    }

                    //Found at tail
                    else if(n == tail)
                    {
                        tail = n->prev;
                        tail->next = endNode;
                        endNode->prev = tail;
                    }

                    //Found in middle
                    else
                    {
                        n->prev->next = n->next;
                        n->next->prev = n->prev;
                    }

                    delete n;

                    mapSize -= 1;
                }

                else
                {
                   throw std::out_of_range("Out Of Range...");
                }
            };

            void clear()
            {
                Node* temp;

                while(head != nullptr && head != endNode)
                {
                    temp = head;

                    head = head->next;

                    delete temp;
                }

                head = nullptr;
                tail = nullptr;

                mapSize = 0;
            };

            friend bool operator==(const Map &m1, const Map &m2)
            {
                if(m1.size() != m2.size())
                {
                    return false;
                }

                for(auto i1 = m1.begin(), i2 = m2.begin(); 
                    i1 != m1.end() && i2 != m2.end() && i1.getNode() != nullptr && i2.getNode() != nullptr;
                    i1++, i2++)
                {
                    if(!(i1.getNode()->val->first == i2.getNode()->val->first) || !(i1.getNode()->val->second == i2.getNode()->val->second))
                    {
                        return false;
                    }
                }

                return true;
            };

            friend bool operator!=(const Map &m1, const Map &m2)
            {
                return !(m1 == m2);
            };

            friend bool operator<(const Map &m1, const Map &m2)
            {
                bool ret = false;

                if(m1.size() >= m2.size())
                {
                    for(auto i1 = m1.begin(), i2 = m2.begin(); 
                        i1 != m1.end() && i2 != m2.end() && i1.getNode() != nullptr && i2.getNode() != nullptr;
                        i1++, i2++)
                    {
                        if(*(i1.getNode()->val) < *(i2.getNode()->val))
                        {
                            ret = true;
                        }
                    }
                }

                else
                {
                    ret = true;

                    for(auto i1 = m1.begin(), i2 = m2.begin(); 
                        i1 != m1.end() && i2 != m2.end() && i1.getNode() != nullptr && i2.getNode() != nullptr;
                        i1++, i2++)
                    {
                        if(*(i1.getNode()->val) != *(i2.getNode()->val))
                        {
                            ret = false;
                        }
                    }

                    for(auto i1 = m1.begin(), i2 = m2.begin(); 
                        i1 != m1.end() && i2 != m2.end() && i1.getNode() != nullptr && i2.getNode() != nullptr;
                        i1++, i2++)
                    {
                        if(*(i1.getNode()->val) < *(i2.getNode()->val))
                        {
                            ret = true;
                        }
                    }
                }

                return ret;
            };

            friend bool operator==(const Iterator &i1, const Iterator &i2)
            {
                return (i1.getNode() == i2.getNode());
            };

            friend bool operator==(const ConstIterator &i1, const ConstIterator &i2)
            {
                return (i1.getNode() == i2.getNode());
            };

            friend bool operator==(const Iterator &i1, const ConstIterator &i2)
            {
                return (i1.getNode() == i2.getNode());
            };

            friend bool operator==(const ConstIterator &i1, const Iterator &i2)
            {
                return (i1.getNode() == i2.getNode());
            };

            friend bool operator!=(const Iterator &i1, const Iterator &i2)
            {
                return (i1.getNode() != i2.getNode());
            };

            friend bool operator!=(const ConstIterator &i1, const ConstIterator &i2)
            {
                return (i1.getNode() != i2.getNode());
            };

            friend bool operator!=(const Iterator &i1, const ConstIterator &i2)
            {
                return (i1.getNode() != i2.getNode());
            };

            friend bool operator!=(const ConstIterator &i1, const Iterator &i2)
            {
                return (i1.getNode() != i2.getNode());
            };

            friend bool operator==(const ReverseIterator &i1, const ReverseIterator &i2)
            {
                return (i1.getNode() == i2.getNode());
            };

            friend bool operator!=(const ReverseIterator &i1, const ReverseIterator &i2)
            {
                return (i1.getNode() != i2.getNode());
            };

        private:
            Node* head;
            Node* tail;
            Node* endNode;
            Node* baseNode;

            size_t mapSize;
    };  
}